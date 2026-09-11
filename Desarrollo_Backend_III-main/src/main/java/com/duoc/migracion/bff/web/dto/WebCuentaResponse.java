package com.duoc.migracion.bff.web.dto;

public record WebCuentaResponse(
        Long id,
        String nombre,
        Double saldo,
        Integer edad,
        String tipo,
        String estado
) {}
