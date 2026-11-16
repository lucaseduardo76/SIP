package com.ifba.sipapi.integracao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração do envio de e-mails.
 *
 * O fluxo completo é exercitado sem substituições: a API publica o pedido de envio
 * em um tópico do Kafka, o consumidor do sistema lê a mensagem, monta o e-mail e o
 * envia por SMTP. Um servidor SMTP de teste (FakeSmtpServer) recebe a mensagem, e o
 * teste usa o link contido no e-mail para concluir a operação pela API.
 *
 * Requisitos: PostgreSQL e Kafka acessíveis (por exemplo, os serviços do docker-compose).
 * Execução: mvn test -Dtest=EmailIntegracaoTest -Dintegracao=true
 * Gera o arquivo target/integracao-email.md com o registro da execução.
 */
@EnabledIfSystemProperty(named = "integracao", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.mail.host=localhost",
        "spring.mail.properties.mail.smtp.auth=false",
        "spring.mail.properties.mail.smtp.starttls.enable=false",
        "spring.mail.properties.mail.smtp.starttls.required=false",
        "application.front-end.url=http://localhost:3000"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmailIntegracaoTest {

    static final FakeSmtpServer smtp;
    static {
        try { smtp = new FakeSmtpServer(); } catch (Exception e) { throw new IllegalStateException(e); }
    }

    @DynamicPropertySource
    static void propriedades(DynamicPropertyRegistry r) {
        r.add("spring.mail.port", smtp::porta);
    }

    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;
    @Autowired KafkaListenerEndpointRegistry registry;

    static final ObjectMapper om = new ObjectMapper();
    static PrintWriter out;
    static final String SENHA = "Senha@123";

    @BeforeAll
    static void abrir() throws Exception {
        out = new PrintWriter(new FileWriter("target/integracao-email.md"));
        out.println("# Testes de integração do envio de e-mails\n");
    }

    @AfterAll
    static void fechar() throws Exception {
        out.close();
        smtp.close();
    }

    @BeforeEach
    void preparar() throws Exception {
        http.getRestTemplate().setRequestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory());
        aguardarConsumidoresKafka();
        smtp.setAtrasoMs(0);
        smtp.limpar();
    }

    // ---------------------------------------------------------------- testes

    @Test
    @Order(1)
    @DisplayName("Cadastro: e-mail de verificação enviado via Kafka e SMTP e conta ativada pelo link")
    void cadastroEnviaEmailDeVerificacao() throws Exception {
        titulo("Teste 1 - Verificação de conta por e-mail (RF01)");
        String email = "maria.teste@ifba.edu.br";

        registrar("Maria Teste", "52998224725", email);
        assertThat(status(email)).isEqualTo("NOT_VERIFIED");
        registro("Usuário cadastrado com situação NOT_VERIFIED.");

        FakeSmtpServer.Mensagem msg = smtp.aguardar(30);
        assertThat(msg).as("e-mail de verificação recebido pelo servidor SMTP").isNotNull();
        MimeMessage mime = ler(msg);
        String token = extrairToken(html(mime));
        registroEmail(msg, mime);

        assertThat(msg.destinatarios()).containsExactly(email);
        assertThat(mime.getSubject()).isEqualTo("Verificação de conta - SIP");

        call(HttpMethod.POST, "/authentication/login", m("email", email, "password", SENHA), 403,
                "Login antes da verificação");

        call(HttpMethod.POST, "/user/account/verify/" + token, null, 200,
                "Verificação da conta usando o link recebido por e-mail");
        assertThat(status(email)).isEqualTo("ACTIVE");
        registro("Situação da conta após a verificação: ACTIVE.");

        call(HttpMethod.POST, "/authentication/login", m("email", email, "password", SENHA), 200,
                "Login após a verificação");
    }

    @Test
    @Order(2)
    @DisplayName("Recuperação de senha: e-mail enviado via Kafka e SMTP e senha redefinida pelo link")
    void recuperacaoDeSenhaEnviaEmail() throws Exception {
        titulo("Teste 2 - Recuperação de senha por e-mail (RF03)");
        String email = "maria.teste@ifba.edu.br";
        String novaSenha = "NovaSenha@456";

        call(HttpMethod.POST, "/user/account/password-recovery/" + email, null, 200,
                "Pedido de recuperação de senha");

        FakeSmtpServer.Mensagem msg = smtp.aguardar(30);
        assertThat(msg).as("e-mail de recuperação recebido pelo servidor SMTP").isNotNull();
        MimeMessage mime = ler(msg);
        String token = extrairToken(html(mime));
        registroEmail(msg, mime);

        assertThat(msg.destinatarios()).containsExactly(email);
        assertThat(mime.getSubject()).isEqualTo("Recuperação de senha - SIP");

        call(HttpMethod.POST, "/user/account/password-reset", m("token", token, "password", novaSenha), 200,
                "Redefinição da senha usando o link recebido por e-mail");

        call(HttpMethod.POST, "/authentication/login", m("email", email, "password", SENHA), 403,
                "Login com a senha antiga");
        call(HttpMethod.POST, "/authentication/login", m("email", email, "password", novaSenha), 200,
                "Login com a nova senha");
    }

    @Test
    @Order(3)
    @DisplayName("Envio assíncrono: a resposta da API não espera o servidor de e-mail")
    void envioDeEmailNaoAtrasaResposta() throws Exception {
        titulo("Teste 3 - Envio assíncrono de e-mails (RNF06)");
        long atraso = 5000;
        smtp.setAtrasoMs(atraso);
        registro("Servidor SMTP configurado para levar " + atraso / 1000 + " segundos para confirmar cada e-mail.");

        String email = "joao.teste@ifba.edu.br";
        long inicio = System.currentTimeMillis();
        registrar("João Teste", "11144477735", email);
        long tempoResposta = System.currentTimeMillis() - inicio;
        registro("Tempo de resposta do cadastro: " + tempoResposta + " ms.");
        assertThat(tempoResposta).as("a resposta não deve aguardar o envio do e-mail").isLessThan(atraso);
        assertThat(smtp.quantidadeNaCaixa()).as("e-mail ainda não confirmado no momento da resposta").isZero();

        FakeSmtpServer.Mensagem msg = smtp.aguardar(30);
        assertThat(msg).as("e-mail entregue depois da resposta").isNotNull();
        long entregueApos = msg.recebidaEm() - inicio;
        registro("E-mail confirmado pelo servidor SMTP " + entregueApos + " ms após o início do cadastro.");
        assertThat(entregueApos).isGreaterThan(tempoResposta);
        assertThat(msg.destinatarios()).containsExactly(email);
    }

    @Test
    @Order(4)
    @DisplayName("Bloqueio e reativação: conta bloqueada após 5 tentativas e reativada pelo link do e-mail")
    void bloqueioEReativacaoPorEmail() throws Exception {
        titulo("Teste 4 - Bloqueio após cinco tentativas e reativação da conta por e-mail (RNF03 e RF03)");
        String email = "maria.teste@ifba.edu.br";
        String senha = "NovaSenha@456"; // definida no teste 2

        for (int i = 1; i <= 5; i++)
            call(HttpMethod.POST, "/authentication/login", m("email", email, "password", "Errada@" + i), 403,
                    "Tentativa de login " + i + " com senha errada");
        assertThat(status(email)).isEqualTo("BLOCKED");
        registro("Situação da conta após cinco tentativas sem sucesso: BLOCKED.");
        call(HttpMethod.POST, "/authentication/login", m("email", email, "password", senha), 403,
                "Login com a senha correta enquanto a conta está bloqueada");

        call(HttpMethod.POST, "/user/account/request-reactivation/" + email, null, 200,
                "Pedido de reativação da conta");
        FakeSmtpServer.Mensagem msg = smtp.aguardar(30);
        assertThat(msg).as("e-mail de reativação recebido pelo servidor SMTP").isNotNull();
        MimeMessage mime = ler(msg);
        String token = extrairToken(html(mime));
        registroEmail(msg, mime);
        assertThat(msg.destinatarios()).containsExactly(email);
        assertThat(mime.getSubject()).isEqualTo("Reativação de conta - SIP");

        call(HttpMethod.POST, "/user/account/reactivate/" + token, null, 200,
                "Reativação da conta usando o link recebido por e-mail");
        assertThat(status(email)).isEqualTo("ACTIVE");
        registro("Situação da conta após a reativação: ACTIVE.");
        call(HttpMethod.POST, "/authentication/login", m("email", email, "password", senha), 200,
                "Login após a reativação");
    }

    // ---------------------------------------------------------------- utilitários

    void aguardarConsumidoresKafka() throws InterruptedException {
        long limite = System.currentTimeMillis() + 60_000;
        while (System.currentTimeMillis() < limite) {
            boolean prontos = !registry.getListenerContainers().isEmpty();
            for (MessageListenerContainer c : registry.getListenerContainers()) {
                if (c.getAssignedPartitions() == null || c.getAssignedPartitions().isEmpty()) prontos = false;
            }
            if (prontos) return;
            Thread.sleep(500);
        }
        throw new IllegalStateException("Consumidores do Kafka não receberam partições a tempo");
    }

    static Map<String, Object> m(Object... kv) {
        Map<String, Object> r = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) r.put((String) kv[i], kv[i + 1]);
        return r;
    }

    void registrar(String nome, String cpf, String email) throws Exception {
        call(HttpMethod.POST, "/authentication/register",
                m("name", nome, "cpf", cpf, "email", email, "password", SENHA, "phone", "73999990000"), 201,
                "Cadastro de " + nome);
    }

    String status(String email) {
        return jdbc.queryForObject("select status_member from tb_user where email=?", String.class, email);
    }

    JsonNode call(HttpMethod metodo, String path, Object body, int esperado, String descricao) throws Exception {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        String json = body == null ? null : om.writeValueAsString(body);
        ResponseEntity<String> r = http.exchange(path, metodo, new HttpEntity<>(json, h), String.class);
        int codigo = r.getStatusCode().value();
        out.println("- " + descricao + ": `" + metodo + " /sip/api" + path.replaceAll("/ey[\\w.-]+", "/<token>")
                + "` → **" + codigo + " " + HttpStatus.valueOf(codigo).getReasonPhrase() + "**");
        out.flush();
        assertThat(codigo).as(descricao).isEqualTo(esperado);
        return r.getBody() == null || r.getBody().isBlank() ? null : om.readTree(r.getBody());
    }

    static MimeMessage ler(FakeSmtpServer.Mensagem msg) throws Exception {
        return new MimeMessage(Session.getInstance(new Properties()),
                new ByteArrayInputStream(msg.conteudo().getBytes(StandardCharsets.UTF_8)));
    }

    static String html(Part p) throws Exception {
        if (p.isMimeType("text/html")) return (String) p.getContent();
        if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                String r = html(bp);
                if (r != null) return r;
            }
        }
        return null;
    }

    static String extrairToken(String html) {
        assertThat(html).as("corpo HTML do e-mail").isNotNull();
        Matcher mt = Pattern.compile("token=([^\"'<&\\s]+)").matcher(html);
        assertThat(mt.find()).as("link com token no corpo do e-mail").isTrue();
        return URLDecoder.decode(mt.group(1), StandardCharsets.UTF_8);
    }

    void titulo(String t) { out.println("\n## " + t + "\n"); out.flush(); }

    void registro(String t) { out.println("- " + t); out.flush(); }

    void registroEmail(FakeSmtpServer.Mensagem msg, MimeMessage mime) throws Exception {
        out.println("- E-mail recebido pelo servidor SMTP: destinatário `" + String.join(", ", msg.destinatarios())
                + "`, assunto \"" + mime.getSubject() + "\", com link contendo o token de acesso.");
        out.flush();
    }
}
