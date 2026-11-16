package com.ifba.sipapi.integracao;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Servidor SMTP mínimo usado apenas nos testes de integração.
 * Recebe as mensagens enviadas pelo sistema por meio do protocolo SMTP
 * e as guarda em memória para que o teste possa verificá-las.
 */
public class FakeSmtpServer implements AutoCloseable {

    /** Mensagem recebida pelo servidor: destinatários e conteúdo bruto (MIME). */
    public record Mensagem(List<String> destinatarios, String conteudo, long recebidaEm) {}

    private final ServerSocket server;
    private final BlockingQueue<Mensagem> caixa = new LinkedBlockingQueue<>();
    private volatile long atrasoMs = 0;
    private volatile boolean ativo = true;

    public FakeSmtpServer() throws IOException {
        server = new ServerSocket(0);
        Thread t = new Thread(this::aceitar, "fake-smtp");
        t.setDaemon(true);
        t.start();
    }

    public int porta() { return server.getLocalPort(); }

    /** Define um atraso na confirmação do recebimento, simulando um servidor de e-mail lento. */
    public void setAtrasoMs(long atrasoMs) { this.atrasoMs = atrasoMs; }

    public Mensagem aguardar(long segundos) throws InterruptedException {
        return caixa.poll(segundos, TimeUnit.SECONDS);
    }

    public int quantidadeNaCaixa() { return caixa.size(); }

    public void limpar() { caixa.clear(); }

    private void aceitar() {
        while (ativo) {
            try {
                Socket s = server.accept();
                Thread t = new Thread(() -> atender(s), "fake-smtp-conn");
                t.setDaemon(true);
                t.start();
            } catch (IOException e) {
                if (ativo) e.printStackTrace();
            }
        }
    }

    private void atender(Socket s) {
        try (s;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             Writer out = new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8)) {
            responder(out, "220 localhost ESMTP teste");
            List<String> destinatarios = new ArrayList<>();
            String linha;
            while ((linha = in.readLine()) != null) {
                String cmd = linha.toUpperCase();
                if (cmd.startsWith("EHLO")) {
                    responder(out, "250-localhost\r\n250 8BITMIME");
                } else if (cmd.startsWith("HELO") || cmd.startsWith("MAIL FROM") || cmd.startsWith("RSET") || cmd.startsWith("NOOP")) {
                    responder(out, "250 OK");
                } else if (cmd.startsWith("RCPT TO")) {
                    destinatarios.add(linha.substring(linha.indexOf('<') + 1, linha.indexOf('>')));
                    responder(out, "250 OK");
                } else if (cmd.equals("DATA")) {
                    responder(out, "354 Envie a mensagem; termine com <CRLF>.<CRLF>");
                    StringBuilder corpo = new StringBuilder();
                    while ((linha = in.readLine()) != null && !linha.equals(".")) {
                        corpo.append(linha.startsWith("..") ? linha.substring(1) : linha).append("\r\n");
                    }
                    if (atrasoMs > 0) Thread.sleep(atrasoMs);
                    caixa.add(new Mensagem(List.copyOf(destinatarios), corpo.toString(), System.currentTimeMillis()));
                    destinatarios.clear();
                    responder(out, "250 OK mensagem recebida");
                } else if (cmd.startsWith("QUIT")) {
                    responder(out, "221 Até logo");
                    return;
                } else {
                    responder(out, "502 Comando não implementado");
                }
            }
        } catch (Exception e) {
            if (ativo) e.printStackTrace();
        }
    }

    private static void responder(Writer out, String msg) throws IOException {
        out.write(msg + "\r\n");
        out.flush();
    }

    @Override
    public void close() throws IOException {
        ativo = false;
        server.close();
    }
}
