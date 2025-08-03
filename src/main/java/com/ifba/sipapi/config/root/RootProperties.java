package com.ifba.sipapi.config.root;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class RootProperties {

    @Value("${sip.root.email}")
    private String email;

    @Value("${sip.root.password}")
    private String password;

    @Value("${sip.root.name}")
    private String name;

    @Value("${sip.root.phone}")
    private String phone;

    @Value("${sip.root.cpf}")
    private String cpf;
}
