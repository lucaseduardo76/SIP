package com.ifba.sipapi.integracao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ifba.sipapi.mail.infra.KafkaApplicationEmailProducer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração do envio de fotografias dos itens (RF06).
 *
 * As imagens são enviadas pela API e gravadas de fato no serviço de armazenamento
 * de objetos configurado em MINIO_ENDPOINT (o MinIO do docker-compose). O teste
 * confere, diretamente no armazenamento, que cada arquivo foi gravado.
 *
 * Requisitos: PostgreSQL e MinIO acessíveis. O envio de e-mails não é usado aqui.
 * Execução: mvn test -Dtest=FotosIntegracaoTest -Dintegracao=true
 * Gera o arquivo target/integracao-fotos.md com o registro da execução.
 */
@EnabledIfSystemProperty(named = "integracao", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.kafka.listener.auto-startup=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FotosIntegracaoTest {

    @Autowired TestRestTemplate http;
    @Autowired JdbcTemplate jdbc;
    @Autowired S3Client s3;
    @MockBean KafkaApplicationEmailProducer emailProducer;

    static final ObjectMapper om = new ObjectMapper();

    @Test
    @DisplayName("Fotografias: três imagens gravadas no armazenamento e a quarta recusada")
    void envioDeFotografias() throws Exception {
        http.getRestTemplate().setRequestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory());
        try (PrintWriter out = new PrintWriter(new FileWriter("target/integracao-fotos.md"))) {
            out.println("# Teste de integração do envio de fotografias (RF06)\n");

            String root = json(post("/authentication/login", null,
                    m("email", "root@sip.com", "password", "SenhaSegura@123"))).get("token").asText();
            post("/authentication/register-admin", root, m("name", "Carla Administradora", "cpf", "52998224725",
                    "email", "carla.admin@sip.com", "phone", "73988887777"));
            jdbc.update("update tb_user set status_member='ACTIVE' where email=?", "carla.admin@sip.com");
            String admin = json(post("/authentication/login", null,
                    m("email", "carla.admin@sip.com", "password", "ifba.52998224725"))).get("token").asText();

            ResponseEntity<String> criado = post("/items/admin/create", admin, m("description", "Estojo azul",
                    "finding_date", LocalDate.now().minusDays(1).toString(), "day_period", "MORNING",
                    "category", "SCHOOL_SUPPLY", "area", "LIBRARY"));
            assertThat(criado.getStatusCode().value()).isEqualTo(201);
            String itemId = json(criado).get("itemId").asText();
            out.println("- Item cadastrado pela administradora: `POST /sip/api/items/admin/create` → **201 Created**");

            ResponseEntity<String> envio = enviarImagens(admin, itemId, 3);
            out.println("- Envio de três imagens PNG: `POST /sip/api/items/admin/images/{itemId}` → **"
                    + envio.getStatusCode().value() + " " + HttpStatus.valueOf(envio.getStatusCode().value()).getReasonPhrase() + "**");
            assertThat(envio.getStatusCode().value()).isEqualTo(201);
            JsonNode urls = json(envio);
            assertThat(urls).hasSize(3);

            for (JsonNode u : urls) {
                String url = u.path("imgUrl").asText();
                if (url.startsWith("http")) url = java.net.URI.create(url).getPath();
                String semBarra = url.startsWith("/") ? url.substring(1) : url;
                String bucket = semBarra.substring(0, semBarra.indexOf('/'));
                String chave = semBarra.substring(semBarra.indexOf('/') + 1);
                HeadObjectResponse head = s3.headObject(b -> b.bucket(bucket).key(chave));
                out.println("- Arquivo conferido no armazenamento: bucket `" + bucket + "`, `" + chave + "`, "
                        + head.contentLength() + " bytes, tipo `" + head.contentType() + "`");
                assertThat(head.contentLength()).isPositive();
            }

            ResponseEntity<String> item = http.exchange("/items/" + itemId, HttpMethod.GET,
                    new HttpEntity<>(headers(admin, MediaType.APPLICATION_JSON)), String.class);
            int qtd = json(item).get("pictures").size();
            out.println("- Consulta do item: `GET /sip/api/items/{itemId}` → **200 OK**, com " + qtd + " fotografias associadas");
            assertThat(qtd).isEqualTo(3);

            ResponseEntity<String> quarta = enviarImagens(admin, itemId, 1);
            out.println("- Envio de uma quarta imagem: → **" + quarta.getStatusCode().value() + " "
                    + HttpStatus.valueOf(quarta.getStatusCode().value()).getReasonPhrase() + "** " + quarta.getBody());
            assertThat(quarta.getStatusCode().value()).isEqualTo(400);
        }
    }

    // ---------------------------------------------------------------- utilitários

    static Map<String, Object> m(Object... kv) {
        Map<String, Object> r = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) r.put((String) kv[i], kv[i + 1]);
        return r;
    }

    HttpHeaders headers(String token, MediaType tipo) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(tipo);
        if (token != null) h.setBearerAuth(token);
        return h;
    }

    ResponseEntity<String> post(String path, String token, Object body) throws Exception {
        return http.exchange(path, HttpMethod.POST,
                new HttpEntity<>(om.writeValueAsString(body), headers(token, MediaType.APPLICATION_JSON)), String.class);
    }

    JsonNode json(ResponseEntity<String> r) throws Exception { return om.readTree(r.getBody()); }

    ResponseEntity<String> enviarImagens(String token, String itemId, int quantidade) throws Exception {
        MultiValueMap<String, Object> corpo = new LinkedMultiValueMap<>();
        for (int i = 0; i < quantidade; i++) {
            byte[] png = imagem(i);
            String nome = "foto" + i + ".png";
            HttpHeaders parte = new HttpHeaders();
            parte.setContentType(MediaType.IMAGE_PNG);
            corpo.add("itemImages", new HttpEntity<>(new ByteArrayResource(png) {
                @Override public String getFilename() { return nome; }
            }, parte));
        }
        return http.exchange("/items/admin/images/" + itemId, HttpMethod.POST,
                new HttpEntity<>(corpo, headers(token, MediaType.MULTIPART_FORM_DATA)), String.class);
    }

    static byte[] imagem(int semente) throws Exception {
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < 64; x++)
            for (int y = 0; y < 64; y++) img.setRGB(x, y, (x * 4 + semente * 40) << 16 | (y * 4) << 8 | 120);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", bos);
        return bos.toByteArray();
    }
}
