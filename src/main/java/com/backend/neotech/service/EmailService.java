package com.backend.neotech.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Código de Recuperação de Senha");
        message.setText("Seu código de recuperação é: " + code + "\n\nEste código expira em 10 minutos.");

        mailSender.send(message);
        System.out.println("Código enviado para o e-mail: " + toEmail);

    }
}
