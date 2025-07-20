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

public @interface UserAPIDocs {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Verifica a conta após Registro", description = "Metodo quando bem sucedido retorna códgio 200 e habilita a conta do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificação feita com sucesso."),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"mensagem qualquer.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface VerifyAccount {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Testa token", description = "Método criado para o front-end validação de token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token valido"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"mensagem qualquer.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface tokenTeste {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Solicita Reativação de Conta", description = "Metodo envia email de reativação de conta para email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email com codigo de reativação sera enviado para email"),
            @ApiResponse(responseCode = "400", description = "Email não existe", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"mensagem qualquer.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface RequestAccountReactivation {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Reativa Conta", description = "Metodo recebe token com codigo de autorização e reativa conta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta reativada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"mensagem qualquer.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface ReactivateAccount {
    }
}
