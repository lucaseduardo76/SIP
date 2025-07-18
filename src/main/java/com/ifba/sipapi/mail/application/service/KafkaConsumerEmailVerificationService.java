package com.ifba.sipapi.mail.application.service;

import com.ifba.sipapi.mail.domain.EmailSender;
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
        topics  = "${spring.kafka.consumer.email.topic}",
        groupId = "${spring.kafka.consumer.email.group-id}",
        containerFactory = "${spring.kafka.consumer.email.factory}"
    )
    public void listen(EmailSender payload) {
        log.info("[start] KafkaConsumerEmailVerification - listen");
        emailService.sendVerificationEmail(payload.getSendTo());
        log.debug("[email-send] E-Mail sent to - {}", payload.getSendTo());
        log.debug("[finish] KafkaConsumerEmailVerification - listen");
    }
}

