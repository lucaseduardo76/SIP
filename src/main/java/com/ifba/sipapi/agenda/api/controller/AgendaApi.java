package com.ifba.sipapi.agenda.api.controller;

import com.ifba.sipapi.agenda.dto.AgendaRequestDto;
import com.ifba.sipapi.docs.swagger.AgendaAPIDocs;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agenda")
@Tag(name = "AgendaApi", description = "Controle responsavel pelas agenda da retirada de itens.")
public interface AgendaApi {

    @AgendaAPIDocs.CreateAgenda
    @PostMapping("/admin/create")
    @ResponseStatus(HttpStatus.CREATED)
    void createItem(
            @Valid @RequestBody AgendaRequestDto agendaRequestDto);

}
