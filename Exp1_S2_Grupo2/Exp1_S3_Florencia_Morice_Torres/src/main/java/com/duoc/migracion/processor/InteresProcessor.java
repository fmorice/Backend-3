package com.duoc.migracion.processor;

import com.duoc.migracion.exception.CuentaNoEncontradaException;
import com.duoc.migracion.exception.InvalidMontoException;
import com.duoc.migracion.exception.RegistroMalClasificadoException;
import com.duoc.migracion.model.Interes;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class InteresProcessor implements ItemProcessor<Interes, Interes> {

    @Override
    public Interes process(Interes item) {
        if (item == null) {
            throw new IllegalArgumentException("Registro de intereses nulo");
        }
        if (item.getCuentaId() == null) {
            throw new CuentaNoEncontradaException("Cuenta inexistente: falta cuenta_id en el registro");
        }
        if (item.getSaldo() == null) {
            throw new InvalidMontoException("Saldo inválido para la cuenta " + item.getCuentaId());
        }

        String tipo = item.getTipo() == null ? "" : item.getTipo().trim().toLowerCase();
        BigDecimal tasa;
        switch (tipo) {
            case "ahorro" -> tasa = new BigDecimal("0.02");
            case "prestamo" -> tasa = new BigDecimal("0.05");
            case "hipoteca" -> tasa = new BigDecimal("0.03");
            default -> throw new RegistroMalClasificadoException("Tipo de cuenta no soportado: " + item.getTipo());
        }

        BigDecimal saldoBase = item.getSaldo();
        BigDecimal interes = saldoBase.multiply(tasa).setScale(2, RoundingMode.HALF_UP);
        item.setSaldo(saldoBase.add(interes).setScale(2, RoundingMode.HALF_UP));
        System.out.println("[InteresProcessor] Cuenta " + item.getCuentaId() + " tipo=" + tipo + " tasa=" + tasa + " interes=" + interes + " saldoFinal=" + item.getSaldo());
        return item;
    }
}
