package com.backend.neotech.exceptions;

// extends : Herança, uma classe herda características de outra classe, ou seja, temos a superclasse
//           e a subclasse. A subclasse é uma "extensão" da superclasse
public class NotFound extends RuntimeException {

    // super: Acessando o construtor da super classe, ou seja, passando parâmetro(s) para a classe mãe.

    public NotFound(String message) {
        super(message);
    }


}