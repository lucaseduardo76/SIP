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
    @Operation(summary = "Solicita Reativação de Conta", description = "Método envia email de reativação de conta para email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email com código de reativação será enviado para email"),
            @ApiResponse(responseCode = "400", description = "Email não existe", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"mensagem qualquer.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface RequestAccountReactivation {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Reativa Conta", description = "Método recebe token com código de autorização e reativa conta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta reativada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou expirado", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"mensagem qualquer.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface ReactivateAccount {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Edição de Telefone e Nome", description = "Método permite a edição de nome e telefone")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados alterados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Verifique os dados e tente novamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"mensagem qualquer.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface Update {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Edição de Telefone e Nome", description = "Método permite a atualização da senha após logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados alterados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Verifique os dados e tente novamente", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"mensagem qualquer.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface UpdatePassword {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Operation(summary = "Recupera a senha do usuário", description = "Método criado para o front-end de recuperação de senha mediante ao envio de e-mail no body do request")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email enviado"),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"mensagem qualquer.\" }"))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"description\": \"INTERNAL SERVER ERROR!\", \"message\": \"POR FAVOR INFORME AO ADMINISTRADOR DO SISTEMA!\" }")))})
    public @interface RecoverPassword {
    }
}
