package com.backend.neotech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication

public class NeotechApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load(); // carrega .env
        System.setProperty("SPRING_USERNAME", dotenv.get("SPRING_USERNAME"));
        System.setProperty("SPRING_PASSWORD", dotenv.get("SPRING_PASSWORD"));
        System.setProperty("SENDGRID_API_KEY", dotenv.get("SENDGRID_API_KEY"));
        System.setProperty("SENDGRID_USERNAME", dotenv.get("SENDGRID_USERNAME"));

        SpringApplication.run(NeotechApplication.class, args);
    }

}
