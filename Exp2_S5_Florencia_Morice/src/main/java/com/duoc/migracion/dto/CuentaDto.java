package com.duoc.migracion.dto;

public record CuentaDto(
        Long cuentaId,
        String nombre,
        Double saldo,
        Integer edad,
        String tipo
) {}