package com.backend.neotech.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity
    @Table(name = "reset_codes")
    public class ResetCode {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "email")
        private String email;

        @Column(name = "code")
        private String code;

        @Column(name = "expiresAt")
        private LocalDateTime expiresAt;

    }

