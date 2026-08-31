package com.duoc.migracion.exception;

public class InvalidDateException extends InvalidCsvRecordException {
    public InvalidDateException(String message) {
        super(message);
    }
}
