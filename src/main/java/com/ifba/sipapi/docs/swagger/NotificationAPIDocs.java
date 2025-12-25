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

public @interface NotificationAPIDocs {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "retornar notificações de um usuario", description = "Metodo quando bem sucedido retorna notificações de um usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificação feita com sucesso."),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"mensagem qualquer.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface getNotificationByUser { }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Seta como lida as notificações enviadas", description = "Metodo quando bem sucedido Seta como lida as notificações enviadas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificação feita com sucesso."),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"mensagem qualquer.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface readNotifications { }

}
