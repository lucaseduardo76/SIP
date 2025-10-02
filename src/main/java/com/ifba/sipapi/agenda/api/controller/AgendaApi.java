package com.ifba.sipapi.agenda.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agenda")
@Tag(name = "AgendaApi", description = "Controle responsavel pelas agenda da retirada de itens.")
public interface AgendaApi {


}
