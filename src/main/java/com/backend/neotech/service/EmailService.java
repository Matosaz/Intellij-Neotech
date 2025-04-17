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
        message.setSubject("Recuperação de Senha - NeoTech");

        message.setText("Olá,\n\n" +
                "Recebemos uma solicitação para redefinir a senha da sua conta NeoTech.\n" +
                "Utilize o código abaixo para continuar com o processo de recuperação:\n\n" +
                "🔐 Código de recuperação: " + code + "\n\n" +
                "Este código é válido por 10 minutos.\n\n" +
                "Se você não solicitou a recuperação de senha, por favor, ignore este e-mail.\n\n" +
                "Atenciosamente,\n" +
                "Equipe NeoTech");


        mailSender.send(message);
        System.out.println("Código enviado para o e-mail: " + toEmail);

    }
}
