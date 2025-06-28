package com.ifba.sipapi;

import com.ifba.sipapi.mail.domain.EmailVerificationDTO;
import com.ifba.sipapi.mail.infra.KafkaApplicationEmailProducer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SipApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SipApiApplication.class, args);
    }

    @Bean
    public CommandLineRunner testKafkaProducer(KafkaApplicationEmailProducer kafkaApplicationEmailProducer) {
        return args -> {
            EmailVerificationDTO payload = EmailVerificationDTO.builder()
                    .to("contatopedrolucascg@gmail.com")
                    .subject("Verify your email")
                    .verificationToken("123456")
                    .build();

            kafkaApplicationEmailProducer.publishEmailVerification(payload);
            System.out.println("✅ Test message sent to Kafka");
        };
    }
}
