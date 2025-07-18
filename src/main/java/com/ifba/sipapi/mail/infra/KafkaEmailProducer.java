package com.ifba.sipapi.mail.infra;

import com.ifba.sipapi.mail.domain.EmailSender;

public interface KafkaEmailProducer {

    void publishEmail(EmailSender EmailSender);
}
