package com.ifba.sipapi.cenarios;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ifba.sipapi.item.api.service.ItemToCharitySchedule;
import com.ifba.sipapi.mail.infra.KafkaApplicationEmailProducer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cenários simulados com dados fictícios, executados contra a API real (HTTP),
 * com banco PostgreSQL. O envio de e-mails (Kafka) é substituído por um mock.
 * Gera o arquivo target/cenarios.md com as requisições e respostas.
 * Requer um PostgreSQL acessível. Execução: mvn test -Dtest=CenariosIntegracaoTest -Dcenarios=true
 */
@EnabledIfSystemProperty(named = "cenarios", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CenariosIntegracaoTest {

    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;
    @Autowired ItemToCharitySchedule charitySchedule;
    @Autowired com.ifba.sipapi.item.api.service.ReservationExpirationSchedule reservationSchedule;
    @MockBean KafkaApplicationEmailProducer emailProducer;
    @LocalServerPort int porta;

    static final ObjectMapper om = new ObjectMapper().findAndRegisterModules().enable(SerializationFeature.INDENT_OUTPUT);
    static PrintWriter out;
    static String root, admin, ana, bruno;

    @BeforeAll
    static void abrir() throws Exception {
        out = new PrintWriter(new FileWriter("target/cenarios.md"));
    }

    @BeforeEach
    void cliente() { http.getRestTemplate().setRequestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory()); }

    @AfterAll
    static void fechar() { out.close(); }

    // ---------- utilitários ----------
    static Map<String, Object> m(Object... kv) {
        Map<String, Object> r = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) r.put((String) kv[i], kv[i + 1]);
        return r;
    }

    JsonNode call(HttpMethod m, String path, String token, Object body, int esperado) throws Exception {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) h.setBearerAuth(token);
        String json = body == null ? null : om.writeValueAsString(body);
        ResponseEntity<String> r = http.exchange(path, m, new HttpEntity<>(json, h), String.class);
        out.println("```http");
        out.println(m + " /sip/api" + path + (token != null ? "\nAuthorization: Bearer <token>" : ""));
        if (json != null) out.println("\n" + json);
        out.println("\n--> " + r.getStatusCode().value() + " " + HttpStatus.valueOf(r.getStatusCode().value()).getReasonPhrase());
        JsonNode node = null;
        if (r.getBody() != null && !r.getBody().isBlank()) {
            try { node = om.readTree(r.getBody()); out.println(om.writeValueAsString(node)); }
            catch (Exception e) { out.println(r.getBody()); }
        }
        out.println("```");
        out.flush();
        assertThat(r.getStatusCode().value()).as(m + " " + path).isEqualTo(esperado);
        return node;
    }

    /** Cliente WebSocket simples: guarda as mensagens recebidas para o teste verificar. */
    class ClienteWs implements WebSocket.Listener {
        final BlockingQueue<String> recebidas = new LinkedBlockingQueue<>();
        final StringBuilder parcial = new StringBuilder();
        final String quem;
        WebSocket ws;
        ClienteWs(String quem, String canal, String token) throws Exception {
            this.quem = quem;
            String url = "ws://localhost:" + porta + "/sip/api/ws/" + canal + "?access_token=" + token;
            ws = HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(URI.create(url), this).get(10, TimeUnit.SECONDS);
            nota("_(" + quem + " conectou-se ao WebSocket em `/sip/api/ws/" + canal + "?access_token=<token>`.)_");
        }
        @Override public CompletionStage<?> onText(WebSocket w, CharSequence data, boolean last) {
            parcial.append(data);
            if (last) { recebidas.add(parcial.toString()); parcial.setLength(0); }
            w.request(1);
            return null;
        }
        /** Aguarda uma mensagem do tipo informado, registrando-a no relatório. */
        JsonNode aguardar(String tipo) throws Exception {
            long limite = System.currentTimeMillis() + 10_000;
            while (System.currentTimeMillis() < limite) {
                String msg = recebidas.poll(500, TimeUnit.MILLISECONDS);
                if (msg == null) continue;
                JsonNode n = om.readTree(msg);
                if (tipo.equalsIgnoreCase(n.path("type").asText())) {
                    out.println("Mensagem recebida por " + quem + " pelo WebSocket, sem nova requisição:");
                    out.println("```json");
                    out.println(om.writeValueAsString(n));
                    out.println("```");
                    out.flush();
                    return n;
                }
            }
            throw new AssertionError(quem + " não recebeu aviso " + tipo + " pelo WebSocket");
        }
        void fechar() { ws.sendClose(WebSocket.NORMAL_CLOSURE, "fim").join(); }
    }

    void titulo(String t) { out.println("\n## " + t + "\n"); }
    void nota(String t) { out.println("\n" + t + "\n"); }

    String login(String email, String senha) throws Exception {
        JsonNode r = call(HttpMethod.POST, "/authentication/login", null, m("email", email, "password", senha), 200);
        return r.get("token").asText();
    }

    void registrarComum(String nome, String cpf, String email) throws Exception {
        call(HttpMethod.POST, "/authentication/register", null, m(
                "name", nome, "cpf", cpf, "email", email, "password", "Senha@123", "phone", "73999990000"), 201);
        // simula a confirmação da conta pelo link enviado por e-mail
        jdbc.update("update tb_user set status_member='ACTIVE' where email=?", email);
        nota("_(Conta confirmada, simulando o acesso ao link enviado por e-mail.)_");
    }

    String criarItem(String token, String descricao, LocalDate data, String categoria) throws Exception {
        JsonNode r = call(HttpMethod.POST, "/items/admin/create", token, m(
                "description", descricao, "finding_date", data.toString(), "day_period", "MORNING",
                "category", categoria, "area", "LIBRARY"), 201);
        return r.get("itemId").asText();
    }

    String pedir(String token, String email, String itemId, String descricao) throws Exception {
        LocalDateTime quando = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("itemId", itemId); body.put("email", email);
        body.put("description", descricao); body.put("dateTime", quando.toString());
        call(HttpMethod.POST, "/items/recovery/withdrawal-requests", token, body, 201);
        return jdbc.queryForObject(
                "select r.id::text from recovery_request r join tb_user u on u.id=r.user_id where u.email=? and r.item_id=?::uuid",
                String.class, email, itemId);
    }

    void analisar(String token, String idRecovery, String status, int esperado) throws Exception {
        call(HttpMethod.POST, "/items/admin/recovery/withdrawal-requests/review", token,
                m("idRecovery", idRecovery, "statusRecovery", status), esperado);
    }

    // ---------- preparação ----------
    @Test @Order(0)
    void preparacao() throws Exception {
        titulo("Preparação: administrador, usuários e agenda");
        root = login("root@sip.com", "SenhaSegura@123");
        call(HttpMethod.POST, "/authentication/register-admin", root, m(
                "name", "Carla Administradora", "cpf", "52998224725", "email", "carla.admin@sip.com",
                "phone", "73988887777"), 201);
        nota("_(A senha inicial do administrador segue o padrão definido pelo sistema.)_");
        jdbc.update("update tb_user set status_member='ACTIVE' where email=?", "carla.admin@sip.com");
        admin = login("carla.admin@sip.com", "ifba.52998224725");

        registrarComum("Ana Souza", "11144477735", "ana.souza@ifba.edu.br");
        registrarComum("Bruno Lima", "39053344705", "bruno.lima@ifba.edu.br");
        ana = login("ana.souza@ifba.edu.br", "Senha@123");
        bruno = login("bruno.lima@ifba.edu.br", "Senha@123");

        List<Map<String, Object>> dias = new ArrayList<>();
        for (String d : List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"))
            dias.add(m("availableDay", d, "availableTimeSlotRequest",
                    List.of(m("startTime", "08:00", "endTime", "12:00"))));
        call(HttpMethod.PATCH, "/agenda/admin", admin, m("availableDayTimeSlotRequest", dias), 200);
    }

    // ---------- cenário 1 ----------
    @Test @Order(1)
    void cenario1_devolucaoCompleta() throws Exception {
        titulo("Cenário 1: cadastro do item, pedido, aprovação, retirada e devolução");
        ClienteWs wsCarla = new ClienteWs("Carla", "admin", admin);
        ClienteWs wsAna = new ClienteWs("Ana", "common", ana);
        String item = criarItem(admin, "Garrafa térmica azul com adesivo", LocalDate.now().minusDays(2), "BOTTLE");
        wsAna.aguardar("NEW_ITEM_CREATED");
        nota("Ana procura o objeto usando os filtros da consulta (nome, categoria, situação e período):");
        JsonNode busca = call(HttpMethod.GET, "/items?itemName=Garrafa&category=BOTTLE&status=DISPONIBLE&startPeriod="
                + LocalDate.now().minusDays(7) + "&endPeriod=" + LocalDate.now(), ana, null, 200);
        assertThat(busca.get("content").findValuesAsText("id")).contains(item);
        nota("Filtro que não corresponde ao item (outra categoria):");
        JsonNode vazia = call(HttpMethod.GET, "/items?category=BOOK", ana, null, 200);
        assertThat(vazia.get("content").findValuesAsText("id")).doesNotContain(item);
        String pedido = pedir(ana, "ana.souza@ifba.edu.br", item, "Garrafa azul com um adesivo de banda na lateral");
        wsCarla.aguardar("NEW_REQUEST");
        call(HttpMethod.GET, "/notification/byUser", admin, null, 200);
        analisar(admin, pedido, "APPROVED", 204);
        wsAna.aguardar("REQUEST_APPROVED");
        wsCarla.fechar(); wsAna.fechar();
        JsonNode i = call(HttpMethod.GET, "/items/" + item, ana, null, 200);
        assertThat(i.get("status").asText()).isEqualTo("RESERVED");
        assertThat(i.get("dateReturned").isNull()).isTrue();
        call(HttpMethod.GET, "/notification/byUser", ana, null, 200);
        nota("No dia e horário agendados, Ana comparece e a administradora confirma a entrega:");
        call(HttpMethod.POST, "/items/admin/recovery/withdrawal-requests/" + pedido + "/deliver", admin, null, 204);
        JsonNode depois = call(HttpMethod.GET, "/items/" + item, ana, null, 200);
        assertThat(depois.get("status").asText()).isEqualTo("CLAIMED");
        assertThat(depois.get("dateReturned").isNull()).isFalse();
    }

    // ---------- cenário 2 ----------
    @Test @Order(2)
    void cenario2_recusaEOutroAprovado() throws Exception {
        titulo("Cenário 2: pedido recusado e outro pedido do mesmo item aprovado");
        String item = criarItem(admin, "Fone de ouvido preto sem fio", LocalDate.now().minusDays(1), "ELECTRONIC");
        String pedidoBruno = pedir(bruno, "bruno.lima@ifba.edu.br", item, "Fone preto, acho que é o meu");
        String pedidoAna = pedir(ana, "ana.souza@ifba.edu.br", item, "Fone preto com risco na caixa e meu nome na etiqueta");
        analisar(admin, pedidoBruno, "REFUSED", 204);
        call(HttpMethod.GET, "/notification/byUser", bruno, null, 200);
        analisar(admin, pedidoAna, "APPROVED", 204);
        JsonNode i = call(HttpMethod.GET, "/items/" + item, ana, null, 200);
        assertThat(i.get("status").asText()).isEqualTo("RESERVED");
        nota("Tentativa de alterar novamente um pedido já analisado:");
        analisar(admin, pedidoBruno, "APPROVED", 400);

        titulo("Cenário 2 (continuação): recusa automática e histórico de pedidos");
        String mochila = criarItem(admin, "Mochila vermelha", LocalDate.now().minusDays(1), "OTHER");
        String pBruno = pedir(bruno, "bruno.lima@ifba.edu.br", mochila, "Mochila vermelha com chaveiro");
        String pAna = pedir(ana, "ana.souza@ifba.edu.br", mochila, "Mochila vermelha com caderno de cálculo dentro");
        nota("Os dois pedidos estão pendentes. A administradora aprova apenas o pedido de Ana:");
        analisar(admin, pAna, "APPROVED", 204);
        assertThat(jdbc.queryForObject("select status from recovery_request where id = ?::uuid", String.class, pBruno)).isEqualTo("REFUSED");
        nota("Situação do pedido de Bruno no banco, sem nenhuma ação da administradora: REFUSED. Aviso recebido por Bruno:");
        call(HttpMethod.GET, "/notification/byUser", bruno, null, 200);
        nota("Histórico de pedidos: os próprios pedidos de Ana, os pedidos do item e os pedidos de Bruno:");
        JsonNode proprios = call(HttpMethod.GET, "/items/recovery-self?email=ana.souza@ifba.edu.br", ana, null, 200);
        assertThat(proprios.get("content")).isNotEmpty();
        JsonNode porItem = call(HttpMethod.GET, "/items/admin/recovery-by-item/" + mochila, admin, null, 200);
        assertThat(porItem.toString()).contains(pAna).contains(pBruno);
        String idBruno = jdbc.queryForObject("select id::text from tb_user where email=?", String.class, "bruno.lima@ifba.edu.br");
        JsonNode porUsuario = call(HttpMethod.GET, "/items/admin/recovery-by-user/" + idBruno, admin, null, 200);
        assertThat(porUsuario.toString()).contains(pBruno);
    }

    // ---------- cenário 3 ----------
    @Test @Order(3)
    void cenario3_doacao() throws Exception {
        titulo("Cenário 3: item não procurado e separação para doação");
        String item = criarItem(admin, "Casaco cinza tamanho M", LocalDate.now().minusDays(181), "CLOTHING");
        JsonNode i = call(HttpMethod.GET, "/items/" + item, ana, null, 200);
        nota("A data prevista para doação (" + i.get("donationDate").asText() + ") já passou. Pedido em andamento feito por Bruno:");
        pedir(bruno, "bruno.lima@ifba.edu.br", item, "Casaco cinza que esqueci na biblioteca");
        nota("_(Execução da rotina diária de doação, que normalmente ocorre à meia-noite.)_");
        charitySchedule.refreshItemToCharity();
        JsonNode depois = call(HttpMethod.GET, "/items/" + item, ana, null, 200);
        assertThat(depois.get("status").asText()).isEqualTo("CHARITY");
        call(HttpMethod.GET, "/notification/byUser", bruno, null, 200);
        nota("Novo pedido para o item separado para doação:");
        LocalDateTime quando = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);
        call(HttpMethod.POST, "/items/recovery/withdrawal-requests", ana, m("itemId", item,
                "email", "ana.souza@ifba.edu.br", "description", "Casaco", "dateTime", quando.toString()), 400);

        titulo("Cenário 3 (continuação): reserva não retirada no prazo");
        String guarda = criarItem(admin, "Guarda-chuva preto", LocalDate.now().minusDays(3), "OTHER");
        String pedidoAna = pedir(ana, "ana.souza@ifba.edu.br", guarda, "Guarda-chuva preto com cabo de madeira");
        analisar(admin, pedidoAna, "APPROVED", 204);
        nota("_(Simulação da passagem do tempo: a data agendada para a retirada passa a ser três dias atrás.)_");
        jdbc.update("update recovery_request set recovery_date_time = now() - interval '3 days' where id = ?::uuid", pedidoAna);
        nota("_(Execução da rotina diária de reservas vencidas.)_");
        reservationSchedule.expireReservations();
        JsonNode g = call(HttpMethod.GET, "/items/" + guarda, ana, null, 200);
        assertThat(g.get("status").asText()).isEqualTo("DISPONIBLE");
        assertThat(jdbc.queryForObject("select status from recovery_request where id = ?::uuid", String.class, pedidoAna)).isEqualTo("EXPIRED");
        call(HttpMethod.GET, "/notification/byUser", ana, null, 200);
    }

    // ---------- cenário 4 ----------
    @Test @Order(4)
    void cenario4_regrasDeAcesso() throws Exception {
        titulo("Cenário 4: regras de validação e de acesso");
        String item = criarItem(admin, "Caderno universitário verde", LocalDate.now(), "SCHOOL_SUPPLY");
        nota("Data de pedido fora do horário da agenda (20h):");
        LocalDateTime noite = LocalDateTime.now().plusDays(1).withHour(20).withMinute(0).withSecond(0).withNano(0);
        call(HttpMethod.POST, "/items/recovery/withdrawal-requests", ana, m("itemId", item,
                "email", "ana.souza@ifba.edu.br", "description", "Caderno verde", "dateTime", noite.toString()), 400);
        nota("Cadastro de item com data futura:");
        call(HttpMethod.POST, "/items/admin/create", admin, m("description", "Teste", "finding_date",
                LocalDate.now().plusDays(1).toString(), "day_period", "MORNING", "category", "OTHER", "area", "LIBRARY"), 400);
        nota("Usuário comum tentando editar um item, consultar todos os pedidos e alterar a agenda:");
        call(HttpMethod.PUT, "/items/admin/edit/" + item, ana, m("description", "alterado"), 403);
        call(HttpMethod.GET, "/items/admin/recovery", ana, null, 403);
        call(HttpMethod.DELETE, "/agenda/admin?dayOfWeek=SUNDAY", ana, null, 403);
        nota("Acesso sem autenticação:");
        call(HttpMethod.GET, "/items/" + item, null, null, 403);

        nota("Exclusão da própria conta por Bruno, primeiro com a senha errada e depois com a correta:");
        call(HttpMethod.DELETE, "/user/delete-account/bruno.lima@ifba.edu.br", bruno, m("password", "Errada@123"), 400);
        call(HttpMethod.DELETE, "/user/delete-account/bruno.lima@ifba.edu.br", bruno, m("password", "Senha@123"), 200);
        Map<String, Object> anon = jdbc.queryForMap("select name, email, cpf, phone, status_member from tb_user where name like 'excluido-%'");
        nota("Registro após a exclusão: " + anon);
        assertThat(anon.get("status_member")).isEqualTo("DELETED");
        call(HttpMethod.POST, "/authentication/login", null, m("email", "bruno.lima@ifba.edu.br", "password", "Senha@123"), 403);
    }

    // ---------- cenário 5 ----------
    @Test @Order(5)
    void cenario5_administracaoESeguranca() throws Exception {
        titulo("Cenário 5: gestão de administradores e segurança das contas");
        nota("O root cadastra um segundo administrador, consulta a lista e os detalhes e depois o exclui:");
        call(HttpMethod.POST, "/authentication/register-admin", root, m(
                "name", "Diego Administrador", "cpf", "86288366757", "email", "diego.admin@sip.com",
                "phone", "73988886666"), 201);
        JsonNode lista = call(HttpMethod.GET, "/user/root/admin-users/root@sip.com", root, null, 200);
        assertThat(lista.toString()).contains("diego.admin@sip.com").contains("carla.admin@sip.com");
        call(HttpMethod.GET, "/user/root/admin-detail/diego.admin@sip.com", root, null, 200);
        call(HttpMethod.DELETE, "/user/root/delete-admin/diego.admin@sip.com", root, null, 200);
        JsonNode listaDepois = call(HttpMethod.GET, "/user/root/admin-users/root@sip.com", root, null, 200);
        assertThat(listaDepois.toString()).doesNotContain("diego.admin@sip.com");
        nota("Um administrador não pode gerenciar administradores:");
        call(HttpMethod.GET, "/user/root/admin-users/carla.admin@sip.com", admin, null, 403);

        nota("Armazenamento da senha: nova usuária cadastrada e valor gravado no banco:");
        registrarComum("Eva Rocha", "71428793860", "eva.rocha@ifba.edu.br");
        String hash = jdbc.queryForObject("select password from tb_user where email=?", String.class, "eva.rocha@ifba.edu.br");
        nota("Coluna `password` de Eva: `" + hash.substring(0, 7) + "…` (" + hash.length() + " caracteres; a senha `Senha@123` não aparece em texto puro).");
        assertThat(hash).startsWith("$2").hasSize(60).doesNotContain("Senha@123");

        nota("Bloqueio após cinco tentativas de login sem sucesso:");
        for (int i = 1; i <= 5; i++)
            call(HttpMethod.POST, "/authentication/login", null, m("email", "eva.rocha@ifba.edu.br", "password", "Errada@" + i), 403);
        assertThat(jdbc.queryForObject("select status_member from tb_user where email=?", String.class, "eva.rocha@ifba.edu.br")).isEqualTo("BLOCKED");
        nota("Situação da conta no banco: BLOCKED. Mesmo com a senha correta, o login é recusado:");
        call(HttpMethod.POST, "/authentication/login", null, m("email", "eva.rocha@ifba.edu.br", "password", "Senha@123"), 403);
        nota("Pedido de reativação (o e-mail é simulado neste cenário; a reativação pelo link é verificada no teste de integração 4):");
        call(HttpMethod.POST, "/user/account/request-reactivation/eva.rocha@ifba.edu.br", null, null, 200);

        nota("Documentação OpenAPI da API, gerada pelo springdoc:");
        ResponseEntity<String> docs = http.getForEntity("/v3/api-docs", String.class);
        JsonNode d = om.readTree(docs.getBody());
        out.println("```http\nGET /sip/api/v3/api-docs\n\n--> " + docs.getStatusCode().value() + " OK\n{ \"openapi\": \""
                + d.path("openapi").asText() + "\", \"paths\": { … " + d.path("paths").size() + " rotas documentadas … } }\n```");
        out.flush();
        assertThat(docs.getStatusCode().value()).isEqualTo(200);
        assertThat(d.path("paths").size()).isGreaterThan(20);
    }
}
