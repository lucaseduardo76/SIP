package com.ifba.sipapi.agenda.api.service;


import com.ifba.sipapi.agenda.dto.AgendaRequestDto;
import org.springframework.stereotype.Service;

@Service
public interface AgendaService {
    void receiveFromRequest(AgendaRequestDto agendaRequestDto);
}
