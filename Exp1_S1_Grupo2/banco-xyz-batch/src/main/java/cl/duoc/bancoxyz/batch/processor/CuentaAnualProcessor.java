package cl.duoc.bancoxyz.batch.processor;

import cl.duoc.bancoxyz.batch.model.CuentaAnual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

public class CuentaAnualProcessor implements ItemProcessor<CuentaAnual, CuentaAnual> {
    private static final Logger log = LoggerFactory.getLogger(CuentaAnualProcessor.class);

    @Override
    public CuentaAnual process(CuentaAnual item) throws Exception {
        if (item == null) return null;

        if (item.getMonto() == null) {
            log.info("Registro con monto nulo filtrado: {}", item);
            return null;
        }

        if (item.getMonto().compareTo(BigDecimal.ZERO) == 0) {
            log.info("Registro filtrado por monto 0: {}", item);
            return null;
        }

        return item;
    }
}
