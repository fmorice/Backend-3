package com.duoc.migracion.processor;

import com.duoc.migracion.dto.CuentaAnualCsvDto;
import com.duoc.migracion.exception.InvalidDateException;
import com.duoc.migracion.exception.InvalidMontoException;
import com.duoc.migracion.exception.RegistroMalClasificadoException;
import com.duoc.migracion.model.CuentaAnual;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
public class CuentaAnualProcessor implements ItemProcessor<CuentaAnualCsvDto, CuentaAnual> {

    @Override
    public CuentaAnual process(CuentaAnualCsvDto item) {
        if (item == null) {
            throw new IllegalArgumentException("Registro anual nulo");
        }
        if (item.getCuentaId() == null) {
            throw new IllegalArgumentException("cuenta_id obligatorio en el CSV anual");
        }
        if (item.getFecha() == null || item.getFecha().isBlank()) {
            throw new InvalidDateException("Fecha obligatoria para la cuenta " + item.getCuentaId());
        }
        try {
            LocalDate.parse(item.getFecha());
        } catch (DateTimeParseException e) {
            throw new InvalidDateException("Fecha inválida: " + item.getFecha());
        }
        if (item.getMonto() == null || item.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidMontoException("Monto inválido para la cuenta " + item.getCuentaId() + ": " + item.getMonto());
        }

        String tipo = item.getTransaccion() == null ? "" : item.getTransaccion().trim().toLowerCase();
        if (!tipo.equals("deposito") && !tipo.equals("retiro") && !tipo.equals("compra")) {
            throw new RegistroMalClasificadoException("Tipo de transacción no soportado: " + item.getTransaccion());
        }

        CuentaAnual cuenta = new CuentaAnual();
        cuenta.setCuentaId(item.getCuentaId());
        cuenta.setFecha(item.getFecha());
        cuenta.setTransaccion(tipo);
        cuenta.setMonto(item.getMonto().abs().setScale(2));
        cuenta.setDescripcion(item.getDescripcion());
        cuenta.setAnio(LocalDate.parse(item.getFecha()).getYear());
        cuenta.setTipoRegistro(tipo);
        cuenta.setSaldoAnual(item.getMonto().abs().setScale(2));
        return cuenta;
    }
}