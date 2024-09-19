package com.backend.neotech.exceptions;

import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;

public class ErrorMessage {

    private LocalDateTime timestamp;
    private String [] messages;
    private HttpStatus title;
    private int status;


    // Construtor

    // Método construtor: Normalmente o método construtor possui o mesmo nome da classe. Ele é muito utilizado
    //                    quando temos a necessidade de passar parâmetros para o objeto no momento da criação.
    // Obs: Toda classe já vem com o construtor padrão, o construtor padrão não possui parâmetros
    // Obs2: Uma classe pode ter vários construtores. Quando você criar um construtor que recebe algum parâmetro,
    //       o construtor padrão deixa de existir, devendo ser recriado a critério.

    // Construtor padrão
    /*
    public ErrorMessage() {

     }
   */

    public ErrorMessage(LocalDateTime timestamp, String[] messages, HttpStatus title) {
        this.timestamp = timestamp;
        this.messages = messages;
        this.title = title;
        this.status = title.value();
    }

    // Set e Get

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String[] getMessages() {
        return messages;
    }

    public void setMessages(String[] messages) {
        this.messages = messages;
    }

    public HttpStatus getTitle() {
        return title;
    }

    public void setTitle(HttpStatus title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}