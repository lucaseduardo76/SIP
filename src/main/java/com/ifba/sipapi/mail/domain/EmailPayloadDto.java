package com.ifba.sipapi.mail.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class EmailPayloadDto {
    private String to;
    private String subject;
    private String route;
}
