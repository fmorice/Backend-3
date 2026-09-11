package com.duoc.migracion.bff.mobile.dto;

public record MobileCuentaResponse(
        Long id,
        String nombre,
        Double saldo,
        String tipo
) {}
