package com.duoc.migracion.processor;

import com.duoc.migracion.dto.TransaccionCsvDto;
import com.duoc.migracion.exception.InvalidDateException;
import com.duoc.migracion.exception.InvalidMontoException;
import com.duoc.migracion.exception.RegistroMalClasificadoException;
import com.duoc.migracion.model.Transaccion;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Component
public class TransaccionProcessor implements ItemProcessor<TransaccionCsvDto, Transaccion> {

    @Override
    public Transaccion process(TransaccionCsvDto item) {
        if (item == null) {
            throw new IllegalArgumentException("Registro de transacción nulo");
        }
        if (item.getId() == null) {
            throw new IllegalArgumentException("id obligatorio para la transacción");
        }
        if (item.getFecha() == null || item.getFecha().isBlank()) {
            throw new InvalidDateException("Fecha obligatoria para el registro " + item.getId());
        }
        try {
            LocalDate.parse(item.getFecha());
        } catch (DateTimeParseException e) {
            throw new InvalidDateException("Fecha inválida en la transacción " + item.getId() + ": " + item.getFecha());
        }
        if (item.getMonto() == null || item.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidMontoException("Monto inválido para la transacción " + item.getId() + ": " + item.getMonto());
        }

        String tipo = item.getTipo() == null ? "" : item.getTipo().trim().toLowerCase();
        if (!tipo.equals("debito") && !tipo.equals("credito")) {
            throw new RegistroMalClasificadoException("Tipo de transacción no soportado: " + item.getTipo());
        }

        // El CSV disponible no incluye una columna cuenta_id; el identificador disponible es id.
        // Se mantiene la validación basada en el dato real del archivo para no inventar columnas.
        Transaccion transaccion = new Transaccion();
        transaccion.setId(item.getId());
        transaccion.setFecha(item.getFecha());
        transaccion.setMonto(item.getMonto().setScale(2));
        transaccion.setTipo(tipo);
        return transaccion;
    }
}