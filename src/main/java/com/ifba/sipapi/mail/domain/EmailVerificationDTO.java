package com.ifba.sipapi.mail.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailVerificationDTO implements EmailDataPattern {
    private final String to;

    @Builder.Default
    private final String subject = "Verificação de e-mail SIP";
}
