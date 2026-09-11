package com.duoc.migracion.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CuentaAnualCsvDto {
    private Long cuentaId;
    private String fecha;
    private String transaccion;
    private BigDecimal monto;
    private String descripcion;
}