package com.ifba.sipapi.agenda.api.controller;

import com.ifba.sipapi.agenda.api.service.AgendaService;
import com.ifba.sipapi.agenda.dto.AgendaRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class AgendaApplicationApi implements AgendaApi {
    private final AgendaService agendaService;

    @Override
    public void createItem(AgendaRequestDto agendaRequestDto) {
        log.info("[start] AgendaApplicationApi - createItem");
        agendaService.receiveFromRequest(agendaRequestDto);
        log.debug("[end] AgendaApplicationApi - createItem");
    }
}
