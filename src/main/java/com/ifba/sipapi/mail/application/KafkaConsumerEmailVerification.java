package com.ifba.sipapi.mail.application;

import com.ifba.sipapi.mail.domain.EmailVerificationPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumerEmailVerification {

    private final EmailService emailService;

    @KafkaListener(
            topics = "email-verification",
            groupId = "email-verification-sender",
            containerFactory = "emailVerificationKafkaListenerContainerFactory"
    )
    public void listen(EmailVerificationPayload payload) {
        System.out.println("Received message: " + payload.toString());
        emailService.sendSimpleEmail(payload.to(), payload.subject(), payload.verificationToken());
    }
}

