package com.ifba.sipapi;

import com.ifba.sipapi.mail.domain.EmailVerificationPayload;
import com.ifba.sipapi.mail.infra.KafkaEmailProducer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SipApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SipApiApplication.class, args);
    }

    /*
    @Bean
    public CommandLineRunner testKafkaProducer(KafkaEmailProducer kafkaEmailProducer) {
        return args -> {
            EmailVerificationPayload payload = EmailVerificationPayload.builder()
                    .to("contatopedrolucascg@gmail.com")
                    .subject("Verify your email")
                    .verificationToken("123456")
                    .build();

            kafkaEmailProducer.publishEmailVerification(payload);
            System.out.println("✅ Test message sent to Kafka");
        };
    }
    */
}
