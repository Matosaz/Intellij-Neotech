package com.backend.neotech.service;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
//teste
@Service
public class EmailService {

    @Value("${BREVO_API_KEY}")
    private String brevoApiKey;

    @Value("${BREVO_FROM_EMAIL}")
    private String fromEmail;

    @Value("${BREVO_FROM_NAME}")
    private String fromName;

    // URL CORRETA da API Brevo (atualizada)
    private final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    /**
     * Envia código de redefinição de senha via Brevo API.
     */
    public void sendResetCode(String toEmail, String code) {
        String subject = "Recuperação de Senha - NeoTech";

        String htmlMessage = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head><meta charset="UTF-8"><title>Recuperação de Senha</title>
            <style>
                body { font-family: Arial, sans-serif; background-color: #f4f4f4; color: #333; margin: 0; padding: 20px; }
                .container { background: #fff; border-radius: 8px; padding: 20px; max-width: 600px; margin: 0 auto; }
                .header { background: #5faa84; color: #fff; text-align: center; padding: 10px; border-radius: 8px 8px 0 0; }
                .code { font-size: 24px; font-weight: bold; color: #5faa84; background: #f1f1f1; padding: 15px;
                        border-radius: 5px; margin: 20px 0; text-align: center; letter-spacing: 2px; }
                .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee; color: #666; }
            </style></head>
            <body>
                <div class='container'>
                    <div class='header'><h2>Recuperação de Senha - NeoTech</h2></div>
                    <p>Olá,</p>
                    <p>Recebemos uma solicitação para redefinir a senha da sua conta.</p>
                    <p>Use o código abaixo para continuar:</p>
                    <div class='code'>%s</div>
                    <p>O código é válido por <strong>10 minutos</strong>.</p>
                    <p>Se você não solicitou a recuperação, ignore este e-mail.</p>
                    <div class='footer'>
                        <p>Atenciosamente,<br><strong>Equipe NeoTech</strong></p>
                    </div>
                </div>
            </body></html>
        """.formatted(code);

        sendEmailViaBrevo(toEmail, subject, htmlMessage);
    }

    /**
     * Envia e-mail de confirmação de agendamento.
     */
    public void sendConfirmationEmail(String toEmail, String nomeCliente, java.util.Date dataColeta,
                                      java.sql.Time horaColeta, String endereco, String numero,
                                      String bairro, String cidade, String estado) {

        String subject = "Confirmação de Agendamento - NeoTech";
        SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm");
        sdfData.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));
        sdfHora.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

        String htmlMessage = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head><meta charset="UTF-8"><title>Confirmação de Agendamento</title>
            <style>
                body { font-family: Arial, sans-serif; background: #f4f4f4; color: #333; margin: 0; padding: 20px; }
                .container { background: #fff; border-radius: 8px; padding: 20px; max-width: 600px; margin: 0 auto; }
                .header { background: #5faa84; color: #fff; text-align: center; padding: 10px; border-radius: 8px 8px 0 0; }
                .section { margin: 20px 0; padding: 15px; background: #f9f9f9; border-radius: 5px; }
                .title { color: #2E7D32; font-weight: bold; display: inline-block; width: 100px; }
                .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee; color: #666; }
            </style></head>
            <body>
                <div class='container'>
                    <div class='header'><h2>Confirmação de Agendamento - NeoTech</h2></div>
                    <p>Olá <strong>%s</strong>,</p>
                    <p>Seu agendamento de coleta foi confirmado com sucesso!</p>
                    
                    <div class='section'>
                        <p><span class='title'>Data:</span> %s</p>
                        <p><span class='title'>Horário:</span> %s</p>
                        <p><span class='title'>Endereço:</span> %s, %s</p>
                        <p><span class='title'>Bairro:</span> %s</p>
                        <p><span class='title'>Cidade/UF:</span> %s - %s</p>
                    </div>
                    
                    <p><strong>Importante:</strong> Nossa equipe entrará em contato para confirmar os detalhes finais.</p>
                    
                    <div class='footer'>
                        <p>Atenciosamente,<br><strong>Equipe Neotech</strong></p>
                    </div>
                </div>
            </body></html>
        """.formatted(nomeCliente, sdfData.format(dataColeta), sdfHora.format(horaColeta),
                endereco, numero, bairro, cidade, estado);

        sendEmailViaBrevo(toEmail, subject, htmlMessage);
    }

    /**
     * Versão simplificada do método de confirmação
     */
    public void sendConfirmationEmail(String toEmail, String nomeCliente, java.util.Date dataColeta,
                                      String horaColeta, String enderecoCompleto) {

        String subject = "Confirmação de Agendamento - NeoTech";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"));

        String htmlContent = """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head><meta charset="UTF-8"><title>Confirmação de Agendamento</title>
            <style>
                body { font-family: Arial, sans-serif; background: #f4f4f4; color: #333; margin: 0; padding: 20px; }
                .container { background: #fff; border-radius: 8px; padding: 20px; max-width: 600px; margin: 0 auto; }
                .header { background: #5faa84; color: #fff; text-align: center; padding: 10px; border-radius: 8px 8px 0 0; }
                .info-box { background: #f9f9f9; padding: 15px; border-radius: 5px; margin: 15px 0; }
                .footer { margin-top: 20px; padding-top: 20px; border-top: 1px solid #eee; color: #666; }
            </style></head>
            <body>
                <div class='container'>
                    <div class='header'><h2>Confirmação de Agendamento - NeoTech</h2></div>
                    <p>Olá <strong>%s</strong>,</p>
                    <p>Seu agendamento de coleta foi confirmado!</p>
                    
                    <div class='info-box'>
                        <p><strong>Data:</strong> %s</p>
                        <p><strong>Horário:</strong> %s</p>
                        <p><strong>Endereço:</strong> %s</p>
                    </div>
                    
                    <p>Em breve nossa equipe entrará em contato para confirmar os detalhes.</p>
                    
                    <div class='footer'>
                        <p>Atenciosamente,<br><strong>Equipe Neotech</strong></p>
                    </div>
                </div>
            </body></html>
        """.formatted(nomeCliente, sdf.format(dataColeta), horaColeta, enderecoCompleto);

        sendEmailViaBrevo(toEmail, subject, htmlContent);
    }

    /**
     * Método principal para envio de emails via Brevo API
     */
    private void sendEmailViaBrevo(String toEmail, String subject, String htmlContent) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(BREVO_API_URL);

            // Headers necessários
            request.setHeader("accept", "application/json");
            request.setHeader("content-type", "application/json");
            request.setHeader("api-key", brevoApiKey);

            // Escape do conteúdo HTML para JSON
            String escapedHtmlContent = htmlContent
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");

            String escapedSubject = subject
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"");

            // JSON payload correto para Brevo API
            String jsonPayload = String.format("""
                {
                    "sender": {
                        "email": "%s",
                        "name": "%s"
                    },
                    "to": [
                        {
                            "email": "%s"
                        }
                    ],
                    "subject": "%s",
                    "htmlContent": "%s"
                }
                """, fromEmail, fromName, toEmail, escapedSubject, escapedHtmlContent);

            request.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));

            // Executa a requisição e processa a resposta
            try (CloseableHttpResponse response = client.execute(request)) {
                int statusCode = response.getCode();
                String responseBody = EntityUtils.toString(response.getEntity());

                if (statusCode >= 200 && statusCode < 300) {
                    System.out.println("✅ E-mail enviado com sucesso para: " + toEmail);
                } else {
                    System.err.println("❌ Erro ao enviar e-mail. Status: " + statusCode);
                    System.err.println("Resposta: " + responseBody);
                    throw new RuntimeException("Falha no envio do e-mail. Status: " + statusCode);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar e-mail para: " + toEmail);
            e.printStackTrace();
            throw new RuntimeException("Erro ao enviar e-mail via Brevo API: " + e.getMessage());
        }
    }
}