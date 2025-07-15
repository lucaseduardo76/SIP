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
}
