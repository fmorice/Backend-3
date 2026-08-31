package com.duoc.migracion.exception;

public class CuentaNoEncontradaException extends BatchProcessingException {
    public CuentaNoEncontradaException(String message) {
        super(message);
    }
}
