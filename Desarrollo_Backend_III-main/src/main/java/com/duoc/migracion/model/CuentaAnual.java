package com.duoc.migracion.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "cuentas_anuales")
public class CuentaAnual {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cuenta_id")
    private Long cuentaId;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "tipo_registro")
    private String tipoRegistro;

    @Column(name = "total_movimientos")
    private Long totalMovimientos = 0L;

    @Column(name = "total_depositos", precision = 19, scale = 2)
    private BigDecimal totalDepositos = BigDecimal.ZERO;

    @Column(name = "total_retiros", precision = 19, scale = 2)
    private BigDecimal totalRetiros = BigDecimal.ZERO;

    @Column(name = "saldo_anual", precision = 19, scale = 2)
    private BigDecimal saldoAnual = BigDecimal.ZERO;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "fecha")
    private String fecha;

    @Column(name = "transaccion")
    private String transaccion;

    @Column(name = "monto", precision = 19, scale = 2)
    private BigDecimal monto;
}