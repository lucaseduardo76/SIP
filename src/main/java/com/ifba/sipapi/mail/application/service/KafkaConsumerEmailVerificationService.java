package com.ifba.sipapi.mail.application.service;

import com.ifba.sipapi.mail.domain.EmailVerificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@RequiredArgsConstructor
public class KafkaConsumerEmailVerificationService {

    private final EmailService emailService;

    @KafkaListener(
            topics = "email-verification",
            groupId = "email-verification-sender",
            containerFactory = "emailVerificationKafkaListenerContainerFactory"
    )
    public void listen(EmailVerificationDTO payload) {
        log.info("[start] KafkaConsumerEmailVerification - listen");
        emailService.sendVerificationEmail(payload.getTo(), payload.getSubject(), payload.getVerificationToken());
        log.debug("[email-send] E-Mail sent to - {}", payload.getTo());
        log.debug("[finish] KafkaConsumerEmailVerification - listen");
    }
}

