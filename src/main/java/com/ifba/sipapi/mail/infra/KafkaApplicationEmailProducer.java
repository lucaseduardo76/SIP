package com.ifba.sipapi.mail.infra;

import com.ifba.sipapi.mail.domain.EmailVerificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class KafkaApplicationEmailProducer implements KafkaEmailProducer{

    private final KafkaTemplate<String, EmailVerificationDTO> kafkaTemplate;

    private static final String TOPIC = "email-verification";

    public void publishEmailVerification(EmailVerificationDTO payload) {
        log.info("[start] KafkaEmailProducer - publishEmailVerification");
        kafkaTemplate.send(TOPIC, payload.getTo(), payload);
        log.debug("[finish] KafkaEmailProducer - publishEmailVerification");
    }
}
