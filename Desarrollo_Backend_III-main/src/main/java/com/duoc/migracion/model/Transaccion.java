package com.duoc.migracion.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "transacciones")
public class Transaccion {

    @Id
    private Long id;
    private String fecha;
    private BigDecimal monto;
    private String tipo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}