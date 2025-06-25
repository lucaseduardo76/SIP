package com.ifba.sipapi.mail.infra;

import com.ifba.sipapi.mail.domain.EmailVerificationPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaEmailProducer {

    private final KafkaTemplate<String, EmailVerificationPayload> kafkaTemplate;

    private static final String TOPIC = "email-verification";

    public void publishEmailVerification(EmailVerificationPayload payload) {
        kafkaTemplate.send(TOPIC, payload);
    }
}
