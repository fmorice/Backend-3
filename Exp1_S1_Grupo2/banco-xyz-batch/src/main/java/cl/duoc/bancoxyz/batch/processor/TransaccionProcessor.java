package cl.duoc.bancoxyz.batch.processor;

import org.springframework.batch.item.ItemProcessor;
import cl.duoc.bancoxyz.batch.model.Transaccion;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransaccionProcessor implements ItemProcessor<Transaccion, Transaccion> {
    private static final Logger log = LoggerFactory.getLogger(TransaccionProcessor.class);

    @Override
    public Transaccion process(Transaccion item) throws Exception {
        if (item == null) return null;

        // Regla simple: si monto == 0 consideramos inválido y lo filtramos
        if (item.getMonto() == null) {
            log.info("Registro con monto nulo filtrado: {}", item);
            return null;
        }
        if (item.getMonto().compareTo(BigDecimal.ZERO) == 0) {
            log.info("Registro filtrado por monto 0: {}", item);
            return null;
        }

        // Mantener la transacción
        return item;
    }
}
