package com.ifba.sipapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class SipApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SipApiApplication.class, args);
    }

}
