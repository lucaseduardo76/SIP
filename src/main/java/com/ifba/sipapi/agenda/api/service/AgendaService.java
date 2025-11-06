package com.ifba.sipapi.agenda.api.service;


import com.ifba.sipapi.agenda.dto.AgendaEditRequestDto;
import org.springframework.stereotype.Service;

@Service
public interface AgendaService {
    void createOrEditAgenda(AgendaEditRequestDto agendaEditRequestDto);
}
