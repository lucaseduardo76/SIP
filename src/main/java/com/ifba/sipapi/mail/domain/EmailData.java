package com.ifba.sipapi.mail.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public record EmailData(String to, String code) {
}
