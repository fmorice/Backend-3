package cl.duoc.bancoxyz.batch.processor;

import org.springframework.batch.item.ItemProcessor;
import cl.duoc.bancoxyz.batch.model.Interes;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InteresProcessor implements ItemProcessor<Interes, Interes> {
    private static final Logger log = LoggerFactory.getLogger(InteresProcessor.class);

    @Override
    public Interes process(Interes item) throws Exception {
        if (item == null) return null;

        if (item.getSaldo() == null || item.getSaldo().compareTo(BigDecimal.ZERO) <= 0) {
            log.info("Registro filtrado por saldo <= 0: cuentaId={}", item.getCuentaId());
            return null;
        }

        String tipo = item.getTipo() != null ? item.getTipo().toLowerCase().trim() : "";
        BigDecimal tasa;
        switch (tipo) {
            case "ahorro":
                tasa = new BigDecimal("0.02");
                break;
            case "prestamo":
                tasa = new BigDecimal("0.05");
                break;
            case "hipoteca":
                tasa = new BigDecimal("0.03");
                break;
            default:
                tasa = BigDecimal.ZERO;
        }

        BigDecimal interes = item.getSaldo().multiply(tasa);
        item.setTasa(tasa);
        item.setInteres(interes);
        return item;
    }
}
