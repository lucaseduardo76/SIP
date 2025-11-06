package com.ifba.sipapi.agenda.api.service;


import com.ifba.sipapi.agenda.dto.AgendaEditRequestDto;
import com.ifba.sipapi.agenda.dto.AvailableDayResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AgendaService {
    void createOrEditAgenda(AgendaEditRequestDto agendaEditRequestDto);
    List<AvailableDayResponse> getAgenda();

}
