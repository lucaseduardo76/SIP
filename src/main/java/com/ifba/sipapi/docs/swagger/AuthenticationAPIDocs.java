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

public @interface AuthenticationAPIDocs {


    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Criar um usuario na aplicação", description = "Metodo quando bem sucedido cria um novo usuario no banco de dados, possibilitando o login por meio do mesmo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario criado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"mensagem qualquer.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface Register {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Tenta Fazer Login na Aplicação", description = "Metodo quando bem sucedido gera o token JWT e retorna para o usuario, para uso posteriores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login feito com sucesso."),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"mensagem qualquer.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface Login {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Testa se token", description = "Método criado para o front-end validade de token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token valido"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"mensagem qualquer.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface TokenTeste {
    }
}
