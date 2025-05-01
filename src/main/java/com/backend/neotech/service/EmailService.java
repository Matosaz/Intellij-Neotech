package com.backend.neotech.service;

import org.springframework.beans.factory.annotation.Autowired;
import jakarta.mail.internet.MimeMessage;  // Alteração para Jakarta Mail
import jakarta.mail.MessagingException;  // Adicionando import da exceção
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetCode(String toEmail, String code) throws MailException {
        // Montando o corpo HTML do e-mail
        String htmlMessage = "<!DOCTYPE html>"
                + "<html lang=\"pt-BR\">"
                + "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><title>Recuperação de Senha - NeoTech</title><style>"
                + "body { font-family: Arial, sans-serif; background-color: #f4f4f4; color: #333; margin: 0; padding: 0; }"
                + ".email-container { width: 100%; max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); overflow: hidden; }"
                + ".email-header { background-color: #007BFF; color: #fff; text-align: center; padding: 20px; }"
                + ".email-body { padding: 20px; }"
                + ".code { font-size: 20px; font-weight: bold; color: #5faa84; padding: 10px; background-color: #f1f1f1; border-radius: 4px; margin: 20px 0; }"
                + ".footer { background-color: #f9f9f9; text-align: center; padding: 10px; font-size: 12px; color: #777; }"
                + ".footer a { color: #007BFF; text-decoration: none; }"
                + "</style></head>"
                + "<body><div class=\"email-container\"><div class=\"email-header\"><h1>Recuperação de Senha - NeoTech</h1></div>"
                + "<div class=\"email-body\"><p>Olá,</p><p>Recebemos uma solicitação para redefinir a senha da sua conta NeoTech.</p>"
                + "<p>Utilize o código abaixo para continuar com o processo de recuperação:</p><div class=\"code\">🔐 Código de recuperação: "
                + code + "</div><p>Este código é válido por 10 minutos.</p><p>Se você não solicitou a recuperação de senha, por favor, ignore este e-mail.</p>"
                + "<p>Atenciosamente,</p><p>Equipe NeoTech</p></div><div class=\"footer\"><p>&copy; 2025 NeoTech. Todos os direitos reservados.</p>"
                + "<p><a href=\"https://www.neotech.com.br\">Visite nosso site</a></p></div></div></body></html>";

        try {
            // Criando um MimeMessage
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            // Definindo o conteúdo do e-mail
            helper.setTo(toEmail);
            helper.setSubject("Recuperação de Senha - NeoTech");
            helper.setText(htmlMessage, true); // O segundo parâmetro 'true' define que o conteúdo é HTML

            // Enviando o e-mail
            mailSender.send(message);
            System.out.println("Código enviado para o e-mail: " + toEmail);
        } catch (MessagingException e) {
            System.out.println("Erro ao configurar o e-mail: " + e.getMessage());
            // Aqui você pode tratar ou lançar uma nova exceção
            throw new RuntimeException("Erro ao configurar o e-mail", e); // Exemplo de lançar uma exceção runtime
        } catch (MailException e) {
            System.out.println("Erro ao enviar o e-mail: " + e.getMessage());
            throw e; // Propagar exceção caso ocorra algum erro no envio
        }
    }
}
