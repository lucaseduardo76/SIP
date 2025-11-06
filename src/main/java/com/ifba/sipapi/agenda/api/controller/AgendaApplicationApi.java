package com.ifba.sipapi.agenda.api.controller;

import com.ifba.sipapi.agenda.api.service.AgendaService;
import com.ifba.sipapi.agenda.dto.AgendaEditRequestDto;
import com.ifba.sipapi.agenda.dto.AvailableDayResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Log4j2
public class AgendaApplicationApi implements AgendaApi {
    private final AgendaService agendaService;

    @Override
    public void createOrEditAgenda(AgendaEditRequestDto agendaEditRequestDto) {
        log.info("[start] AgendaApplicationApi - editAgenda");
        agendaService.createOrEditAgenda(agendaEditRequestDto);
        log.debug("[finish] AgendaApplicationApi - editAgenda");
    }

    @Override
    public List<AvailableDayResponse> getAgenda() {
        log.info("[start] AgendaApplicationApi - getAgenda");
        List<AvailableDayResponse> agenda = agendaService.getAgenda();
        log.debug("[finish] AgendaApplicationApi - getAgenda");
        return agenda;
    }
}
