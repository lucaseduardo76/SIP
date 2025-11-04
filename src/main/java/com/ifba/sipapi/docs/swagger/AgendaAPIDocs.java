package com.ifba.sipapi.docs.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public @interface AgendaAPIDocs {


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Cria uma nova Agenda", description = "Método quando bem sucedido retorna código 201 e cria uma nova agenda na base de dados. A agenda contém horários ocupados, dias disponíveis e horários de início e fim do expediente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Agenda criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"Campos obrigatórios ausentes ou inválidos.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface CreateAgenda { }


}
