package com.ifba.sipapi.mail.application.service;

import com.ifba.sipapi.mail.domain.EmailDataPattern;

public interface KafkaEmailConsumer {
    void listen(EmailDataPattern payload);
}
