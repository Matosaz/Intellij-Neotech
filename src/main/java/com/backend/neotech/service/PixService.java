package com.backend.neotech.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
public class PixService {

    private static final String CHAVE_PIX = "442.042.038-33";
    private static final String NOME_RECEBEDOR = "Matheus Pires";
    private static final String CIDADE = "Barueri";
    private static final int QR_CODE_SIZE = 300;

    public String gerarQRCodePix(Double valor) {
        validarValor(valor);
        String payload = gerarPayloadPix(valor);
        try {
            BufferedImage qrImage = gerarQRCodeImagem(payload);
            return converterParaBase64(qrImage);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar QR Code PIX: " + e.getMessage(), e);
        }
    }

    public boolean validarPayload(String payload) {
        if (payload == null || payload.trim().isEmpty()) return false;
        if (!payload.startsWith("000201")) return false;
        if (!payload.matches(".*6304[0-9A-F]{4}$")) return false;

        return payload.contains("br.gov.bcb.pix")
                && payload.contains(CHAVE_PIX)
                && payload.contains(NOME_RECEBEDOR)
                && payload.contains(CIDADE)
                && payload.contains("986")
                && payload.contains("BR");
    }

    public String gerarPayloadPix(Double valor) {
        StringBuilder payload = new StringBuilder();

        // 1. Payload Format Indicator
        payload.append("000201");

        // 2. Point of Initiation Method - REMOVIDO para QR estático
        // QR estático não usa 010212 ou 010211

        // 3. Merchant Account Information
        StringBuilder merchantInfo = new StringBuilder();
        merchantInfo.append("0014br.gov.bcb.pix"); // GUI
        merchantInfo.append(String.format("01%02d%s", CHAVE_PIX.length(), CHAVE_PIX));

        payload.append(String.format("26%02d%s", merchantInfo.length(), merchantInfo.toString()));

        // 4. Merchant Category Code
        payload.append("52040000");

        // 5. Transaction Currency
        payload.append("5303986");

        // 6. Transaction Amount
        if (valor != null && valor > 0) {
            String amount = String.format("%.2f", valor).replace(",", ".");
            // Garante formato correto: 1.00, 0.50, etc.
            if (amount.startsWith(".")) {
                amount = "0" + amount;
            }
            payload.append(String.format("54%02d%s", amount.length(), amount));
        }

        // 7. Country Code
        payload.append("5802BR");

        // 8. Merchant Name (max 25 chars)
        String nomeFormatado = NOME_RECEBEDOR;
        if (nomeFormatado.length() > 25) {
            nomeFormatado = nomeFormatado.substring(0, 25);
        }
        payload.append(String.format("59%02d%s", nomeFormatado.length(), nomeFormatado));

        // 9. Merchant City (max 15 chars)
        String cidadeFormatada = CIDADE;
        if (cidadeFormatada.length() > 15) {
            cidadeFormatada = cidadeFormatada.substring(0, 15);
        }
        payload.append(String.format("60%02d%s", cidadeFormatada.length(), cidadeFormatada));

        // 10. Additional Data Field - CORRIGIDO
        StringBuilder additionalData = new StringBuilder();
        String txid = gerarTransactionId();

        // Formato correto: [ID][length][value]
        additionalData.append(String.format("05%02d%s", txid.length(), txid));

        payload.append(String.format("62%02d%s", additionalData.length(), additionalData.toString()));

        // 11. CRC16
        String dataForCrc = payload.toString() + "6304";
        String crc = calcularCRC16(dataForCrc);
        payload.append("6304").append(crc);

        return payload.toString();
    }

    private String gerarTransactionId() {
        // TXID mais simples - apenas alfanumérico
        return "NT" + System.currentTimeMillis();
    }

    private String calcularCRC16(String data) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;

        byte[] bytes = data.getBytes();
        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }

        crc &= 0xFFFF;
        return String.format("%04X", crc);
    }

    private BufferedImage gerarQRCodeImagem(String payload) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private String converterParaBase64(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    private void validarValor(Double valor) {
        if (valor == null || valor < 0.01 || valor > 5000) {
            throw new IllegalArgumentException("Valor inválido para PIX (R$0,01 - R$5.000,00)");
        }
    }

    // Método para debug do payload
    public Map<String, Object> analisarPayload(String payload) {
        return Map.of(
                "payload", payload,
                "comprimento", payload.length(),
                "inicio", payload.substring(0, Math.min(50, payload.length())),
                "fim", payload.substring(Math.max(0, payload.length() - 10)),
                "crc", payload.substring(payload.length() - 4),
                "estruturaValida", validarPayload(payload)
        );
    }
}