package com.ifba.sipapi.mail.domain;

public record EmailVerificationPayload(
        String to,
        String subject,
        String verificationToken) implements EmailPayload { }
