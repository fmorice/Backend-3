package com.bancoxyz.cliente.model;
public class Cliente {
    private Long id;
    private String rut;
    private String nombre;
    private String email;
    private String tipo;
    public Cliente() {}
    public Cliente(Long id, String rut, String nombre, String email, String tipo) {
        this.id = id; this.rut = rut; this.nombre = nombre; this.email = email; this.tipo = tipo;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
