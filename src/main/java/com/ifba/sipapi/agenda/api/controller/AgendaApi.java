package com.ifba.sipapi.agenda.api.controller;

import com.ifba.sipapi.agenda.domain.DayOfWeekEnum;
import com.ifba.sipapi.agenda.dto.AgendaEditRequestDto;
import com.ifba.sipapi.agenda.dto.AvailableDayResponse;
import com.ifba.sipapi.docs.swagger.AgendaAPIDocs;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agenda")
@Tag(name = "AgendaApi", description = "Controle responsavel pelas agenda da retirada de itens.")
public interface AgendaApi {

    @AgendaAPIDocs.CreateEditAgenda
    @PatchMapping("/admin")
    @ResponseStatus(HttpStatus.OK)
    void createOrEditAgenda(
            @Valid @RequestBody AgendaEditRequestDto agendaEditRequestDto);

    @AgendaAPIDocs.GetAgenda
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    List<AvailableDayResponse> getAgenda();

    @AgendaAPIDocs.DeleteDay
    @DeleteMapping("/admin")
    @ResponseStatus(HttpStatus.OK)
    void deleteDay(@RequestParam DayOfWeekEnum dayOfWeek);
}
