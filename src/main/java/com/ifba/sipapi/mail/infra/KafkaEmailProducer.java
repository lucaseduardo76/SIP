package com.ifba.sipapi.mail.infra;

import com.ifba.sipapi.mail.domain.EmailVerificationDTO;

public interface KafkaEmailProducer {

    void publishEmailVerification(EmailVerificationDTO payload);
}
