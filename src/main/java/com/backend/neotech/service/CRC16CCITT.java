package com.backend.neotech.service;

import java.nio.charset.StandardCharsets;

public class CRC16CCITT {

    private static final int POLYNOMIAL = 0x1021;
    private static final int INITIAL_VALUE = 0xFFFF;

    public static String crc16(String data) {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        int crc = INITIAL_VALUE;

        for (byte b : bytes) {
            crc ^= (b << 8);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ POLYNOMIAL;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFFFF;
            }
        }

        return String.format("%04X", crc);
    }
}
