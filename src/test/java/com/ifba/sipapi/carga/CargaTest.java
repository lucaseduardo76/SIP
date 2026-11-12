package com.ifba.sipapi.carga;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de carga simples: N usuários simultâneos fazem login e consultam itens repetidamente
 * durante um intervalo fixo. São executadas três rodadas (10, 50 e 100 usuários) e, para cada uma,
 * são medidos o tempo médio de resposta, o percentil 95, o tempo máximo, a vazão e a taxa de erros.
 *
 * Os usuários de teste são inseridos diretamente no banco (já ativos), para que nenhum e-mail seja enviado.
 * Requer PostgreSQL acessível (por exemplo, o do docker compose, em um banco separado).
 * Execução: mvn test -Dtest=CargaTest -Dcarga=true
 * Gera o arquivo target/carga.md com os resultados.
 */
@EnabledIfSystemProperty(named = "carga", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "logging.level.root=WARN",
        "logging.level.com.ifba=WARN"
})
class CargaTest {

    static final int[] NIVEIS = {10, 50, 100};
    static final int SEGUNDOS_POR_RODADA = Integer.getInteger("carga.segundos", 30);
    static final String SENHA = "Senha@123";
    static final ObjectMapper om = new ObjectMapper();

    @LocalServerPort int porta;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;

    final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    record Amostra(String operacao, long ms, boolean erro) {}

    @Test
    void cargaEmTresNiveis() throws Exception {
        String base = "http://localhost:" + porta + "/sip/api";
        prepararDados(base);

        try (PrintWriter out = new PrintWriter(new FileWriter("target/carga.md"))) {
            out.println("# Teste de carga do SIP\n");
            out.println("Cada usuário virtual repete, durante " + SEGUNDOS_POR_RODADA
                    + " segundos: login, consulta paginada de itens e consulta de um item.\n");
            out.println("| Usuários simultâneos | Operação | Requisições | Média (ms) | P95 (ms) | Máximo (ms) | Erros (%) | Vazão (req/s) |");
            out.println("|---|---|---|---|---|---|---|---|");
            for (int n : NIVEIS) {
                List<Amostra> amostras = rodada(base, n);
                for (String op : List.of("login", "consulta de itens", "consulta de um item", "total")) {
                    List<Amostra> a = op.equals("total") ? amostras
                            : amostras.stream().filter(x -> x.operacao().equals(op)).toList();
                    long[] tempos = a.stream().mapToLong(Amostra::ms).sorted().toArray();
                    long erros = a.stream().filter(Amostra::erro).count();
                    double media = Arrays.stream(tempos).average().orElse(0);
                    long p95 = tempos.length == 0 ? 0 : tempos[(int) Math.ceil(tempos.length * 0.95) - 1];
                    long max = tempos.length == 0 ? 0 : tempos[tempos.length - 1];
                    out.printf(Locale.ROOT, "| %d | %s | %d | %.0f | %d | %d | %.2f | %.1f |%n",
                            n, op, a.size(), media, p95, max, a.isEmpty() ? 0 : 100.0 * erros / a.size(),
                            a.size() / (double) SEGUNDOS_POR_RODADA);
                }
                out.flush();
            }
        }
    }

    List<Amostra> rodada(String base, int usuarios) throws Exception {
        List<String> itens = jdbc.queryForList("select id::text from item", String.class);
        ConcurrentLinkedQueue<Amostra> amostras = new ConcurrentLinkedQueue<>();
        ExecutorService pool = Executors.newFixedThreadPool(usuarios);
        long fim = System.currentTimeMillis() + SEGUNDOS_POR_RODADA * 1000L;
        CountDownLatch largada = new CountDownLatch(1);
        AtomicInteger seq = new AtomicInteger();
        for (int u = 0; u < usuarios; u++) {
            int id = u;
            pool.submit(() -> {
                Random rnd = new Random(id);
                largada.await();
                while (System.currentTimeMillis() < fim) {
                    String email = "carga" + id + "@ifba.edu.br";
                    String token = medir(amostras, "login", () -> post(base + "/authentication/login",
                            "{\"email\":\"" + email + "\",\"password\":\"" + SENHA + "\"}", null), true);
                    if (token == null) continue;
                    medir(amostras, "consulta de itens", () -> get(base + "/items?page=" + rnd.nextInt(3) + "&size=10", token), false);
                    medir(amostras, "consulta de um item", () -> get(base + "/items/" + itens.get(rnd.nextInt(itens.size())), token), false);
                    seq.incrementAndGet();
                }
                return null;
            });
        }
        largada.countDown();
        pool.shutdown();
        pool.awaitTermination(SEGUNDOS_POR_RODADA + 120L, TimeUnit.SECONDS);
        return new ArrayList<>(amostras);
    }

    interface Chamada { HttpResponse<String> executar() throws Exception; }

    String medir(Queue<Amostra> amostras, String operacao, Chamada chamada, boolean devolveToken) {
        long inicio = System.nanoTime();
        try {
            HttpResponse<String> r = chamada.executar();
            long ms = (System.nanoTime() - inicio) / 1_000_000;
            boolean erro = r.statusCode() >= 400;
            amostras.add(new Amostra(operacao, ms, erro));
            if (devolveToken && !erro) return om.readTree(r.body()).get("token").asText();
            return erro ? null : "";
        } catch (Exception e) {
            amostras.add(new Amostra(operacao, (System.nanoTime() - inicio) / 1_000_000, true));
            return null;
        }
    }

    HttpResponse<String> post(String url, String json, String token) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json));
        if (token != null) b.header("Authorization", "Bearer " + token);
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    HttpResponse<String> get(String url, String token) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    void prepararDados(String base) throws Exception {
        String hash = encoder.encode(SENHA);
        int max = Arrays.stream(NIVEIS).max().orElse(100);
        for (int i = 0; i < max; i++) {
            jdbc.update("""
                    insert into tb_user (id, name, cpf, email, role, status_member, failed_login_attempts, password, phone, account_code, profile_image_url)
                    values (gen_random_uuid(), ?, ?, ?, 'COMMON', 'ACTIVE', 0, ?, '73999990000', '000000', '')
                    """, "Usuario Carga " + i, String.format("%011d", 90000000000L + i), "carga" + i + "@ifba.edu.br", hash);
        }
        HttpResponse<String> login = post(base + "/authentication/login", "{\"email\":\"root@sip.com\",\"password\":\"SenhaSegura@123\"}", null);
        String root = om.readTree(login.body()).get("token").asText();
        jdbc.update("update tb_user set role='ADMIN' where email='carga0@ifba.edu.br'");
        String admin = om.readTree(post(base + "/authentication/login",
                "{\"email\":\"carga0@ifba.edu.br\",\"password\":\"" + SENHA + "\"}", null).body()).get("token").asText();
        assertThat(root).isNotBlank();
        for (int i = 0; i < 30; i++) {
            HttpResponse<String> r = post(base + "/items/admin/create", "{\"description\":\"Objeto de carga " + i
                    + "\",\"finding_date\":\"" + LocalDate.now().minusDays(1) + "\",\"day_period\":\"MORNING\",\"category\":\"OTHER\",\"area\":\"LIBRARY\"}", admin);
            assertThat(r.statusCode()).isEqualTo(201);
        }
    }
}
