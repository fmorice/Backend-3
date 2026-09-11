package com.duoc.migracion.exception;

public class BatchProcessingException extends RuntimeException {
    public BatchProcessingException(String message) {
        super(message);
    }

    public BatchProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
