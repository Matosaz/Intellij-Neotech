package com.backend.neotech.service;

import com.backend.neotech.model.Orcamento;
import com.backend.neotech.repository.OrcamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OrcamentoService {

    private final OrcamentoRepository orcamentoRepository;
    private static final String CHAVE_PIX = "mathusphd20@gmail.com"; // Substitua pela sua chave Pix real
    private static final String NOME_RECEBEDOR = "Matheus Pires";
    private static final String CIDADE = "Barueri";

    @Autowired
    public OrcamentoService(OrcamentoRepository orcamentoRepository) {
        this.orcamentoRepository = orcamentoRepository;
    }

    public Orcamento salvarOrcamento(Orcamento orcamento) {
        return orcamentoRepository.save(orcamento);
    }

    public List<Orcamento> listarOrcamentos() {
        return orcamentoRepository.findAll();
    }

    public List<Orcamento> getOrcamentosByUsuario(Long usuarioId) {
        return orcamentoRepository.findByUsuarioIdWithCategorias(usuarioId);
    }

    public List<Orcamento> getOrcamentosByCategoria(Long categoriaId) {
        return orcamentoRepository.findByCategoriaIdWithCategorias(categoriaId);
    }

    public Optional<Orcamento> buscarPorId(Long id) {
        return orcamentoRepository.findById(id);
    }

    public void deleteOrcamento(Long id) {
        orcamentoRepository.deleteById(id);
    }

    public Map<String, Object> gerarQRCodePix(Double valorTotal) throws Exception {
        String txid = "NEOTECH" + System.currentTimeMillis();
        String payload = gerarPayloadPix(valorTotal, txid);
        String qrCodeBase64 = gerarImagemQRCode(payload);


        String qrCodeDataUrl = "data:image/png;base64," + qrCodeBase64;
        return Map.of(
                "qrCodeBase64", qrCodeBase64,
                "qrCodeDataUrl", qrCodeDataUrl, // Para usar direto no <img>
                "payload", payload,
                "valorTotal", valorTotal,
                "txid", txid,
                "chavePix", CHAVE_PIX,
                "nomeRecebedor", NOME_RECEBEDOR
        );
    }

    private String gerarPayloadPix(Double valorTotal, String txid) throws Exception {
        StringBuilder payload = new StringBuilder();

        // 00 - Payload Format Indicator
        payload.append("000201");
        // 01 - Point of Initiation Method (dinâmico)
        payload.append("010212");

        // 26 - Merchant Account Information
        String gui = "0014br.gov.bcb.pix";
        String chavePixField = "01" + String.format("%02d", CHAVE_PIX.getBytes(StandardCharsets.UTF_8).length) + CHAVE_PIX;
        String merchantAccount = gui + chavePixField;
        payload.append("26").append(String.format("%02d", merchantAccount.getBytes(StandardCharsets.UTF_8).length)).append(merchantAccount);

        // 52 - Merchant Category Code
        payload.append("52040000");
        // 53 - Currency (986 = BRL)
        payload.append("5303986");
        // 54 - Transaction Amount
        String valorStr = String.format("%.2f", valorTotal).replace(",", ".");
        payload.append("54").append(String.format("%02d", valorStr.getBytes(StandardCharsets.UTF_8).length)).append(valorStr);
        // 58 - Country Code
        payload.append("5802BR");
        // 59 - Merchant Name
        payload.append("59").append(String.format("%02d", NOME_RECEBEDOR.getBytes(StandardCharsets.UTF_8).length)).append(NOME_RECEBEDOR);
        // 60 - City
        payload.append("60").append(String.format("%02d", CIDADE.getBytes(StandardCharsets.UTF_8).length)).append(CIDADE);

        // 62 - Additional Data Field (TXID)
        String txidField = "05" + String.format("%02d", txid.getBytes(StandardCharsets.UTF_8).length) + txid;
        payload.append("62").append(String.format("%02d", txidField.getBytes(StandardCharsets.UTF_8).length)).append(txidField);

        // 63 - CRC16
        String payloadParaCRC = payload.toString() + "6304"; // Inclui "6304" no cálculo
        String crc = calcularCRC16(payloadParaCRC);
        payload.append("6304").append(crc);

        return payload.toString();
    }

    // ================== VALIDAÇÃO DO PIX ==================

    public boolean validarPayloadPix(String payload) {
        try {
            if (!payload.startsWith("000201")) return false;
            if (!payload.substring(payload.length() - 8, payload.length() - 4).equals("6304")) return false;

            String crcInformado = payload.substring(payload.length() - 4);
            String payloadSemCRC = payload.substring(0, payload.length() - 4);

            // CRC16 correto
            String crcCalculado = calcularCRC16(payloadSemCRC + "6304");
            if (!crcCalculado.equalsIgnoreCase(crcInformado)) return false;

            // Verifica TXID
            int index62 = payload.indexOf("62");
            if (index62 == -1) return false;
            int length62 = Integer.parseInt(payload.substring(index62 + 2, index62 + 4));
            String additionalDataField = payload.substring(index62 + 4, index62 + 4 + length62);
            if (!additionalDataField.startsWith("05")) return false;
            int lengthTxid = Integer.parseInt(additionalDataField.substring(2, 4));
            String txid = additionalDataField.substring(4);
            return txid.length() == lengthTxid;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================== CRC16 ==================

    private String calcularCRC16(String data) {
        int crc = 0xFFFF;
        for (int i = 0; i < data.length(); i++) {
            crc ^= (data.charAt(i) << 8);
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x8000) != 0) crc = (crc << 1) ^ 0x1021;
                else crc <<= 1;
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }

    // ================== QR CODE ==================

    private String gerarImagemQRCode(String payload) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        var bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, 300, 300);
        BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(qrImage, "PNG", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
}