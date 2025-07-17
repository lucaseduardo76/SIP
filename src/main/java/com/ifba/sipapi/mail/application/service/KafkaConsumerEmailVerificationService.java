package com.ifba.sipapi.mail.application.service;

import com.ifba.sipapi.mail.domain.EmailVerificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class KafkaConsumerEmailVerificationService implements KafkaConsumerEmail {

    private final EmailService emailService;

    @KafkaListener(
        topics  = "${spring.kafka.consumer.email-verification.topic}",
        groupId = "${spring.kafka.consumer.email-verification.group-id}",
        containerFactory = "${spring.kafka.consumer.email-verification.factory}"
    )
    public void listen(EmailVerificationDTO payload) {
        log.info("[start] KafkaConsumerEmailVerification - listen");
        emailService.sendVerificationEmail(payload.getTo(), payload.getSubject());
        log.debug("[email-send] E-Mail sent to - {}", payload.getTo());
        log.debug("[finish] KafkaConsumerEmailVerification - listen");
    }
}

