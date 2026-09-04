package com.duoc.migracion.bff.atm;

public record SaldoAtmDto(
        Long cuentaId,
        Double saldo
) {}
