package com.duoc.migracion.exception;

public class InvalidCsvRecordException extends BatchProcessingException {
    public InvalidCsvRecordException(String message) {
        super(message);
    }

    public InvalidCsvRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}
