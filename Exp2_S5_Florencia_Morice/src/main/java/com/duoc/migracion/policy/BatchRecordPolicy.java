package com.duoc.migracion.policy;

import com.duoc.migracion.exception.*;
import org.springframework.stereotype.Component;

@Component
public class BatchRecordPolicy {

    public boolean shouldSkip(Throwable throwable) {
        return throwable instanceof InvalidCsvRecordException
                || throwable instanceof IllegalArgumentException;
    }

    public boolean shouldRetry(Throwable throwable) {
        return throwable instanceof CuentaNoEncontradaException;
    }

    public String classify(Throwable throwable) {
        if (throwable instanceof InvalidMontoException) {
            return "Monto inválido";
        }
        if (throwable instanceof InvalidDateException) {
            return "Fecha inválida";
        }
        if (throwable instanceof RegistroMalClasificadoException) {
            return "Registro mal clasificado";
        }
        if (throwable instanceof CuentaNoEncontradaException) {
            return "Cuenta inexistente";
        }
        if (throwable instanceof InvalidCsvRecordException) {
            return "CSV inválido";
        }
        return "Error no controlado";
    }
}
