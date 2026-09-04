package com.duoc.migracion.bff.mobile;

public record CuentaMobileDto(
        Long cuentaId,
        String nombre,
        Double saldo,
        String tipo
) {}
