package com.duoc.migracion.writer;

import com.duoc.migracion.model.CuentaAnual;
import com.duoc.migracion.repository.CuentaAnualRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class CuentaAnualWriter implements ItemWriter<CuentaAnual> {

    private final CuentaAnualRepository cuentaAnualRepository;

    public CuentaAnualWriter(CuentaAnualRepository cuentaAnualRepository) {
        this.cuentaAnualRepository = cuentaAnualRepository;
    }

    @Override
    public void write(@NonNull Chunk<? extends CuentaAnual> chunk) {
        Map<String, CuentaAnual> resumenAnual = new HashMap<>();

        for (CuentaAnual item : chunk) {
            int anio = LocalDate.parse(item.getFecha()).getYear();
            String key = item.getCuentaId() + "-" + anio;
            CuentaAnual resumen = resumenAnual.computeIfAbsent(key, k -> {
                CuentaAnual nueva = new CuentaAnual();
                nueva.setCuentaId(item.getCuentaId());
                nueva.setAnio(anio);
                nueva.setDescripcion("Estado anual para auditoría");
                nueva.setTotalMovimientos(0L);
                nueva.setTotalDepositos(BigDecimal.ZERO);
                nueva.setTotalRetiros(BigDecimal.ZERO);
                nueva.setSaldoAnual(BigDecimal.ZERO);
                return nueva;
            });

            resumen.setTotalMovimientos(resumen.getTotalMovimientos() + 1L);
            BigDecimal monto = item.getMonto() == null ? BigDecimal.ZERO : item.getMonto();
            if ("deposito".equalsIgnoreCase(item.getTransaccion())) {
                resumen.setTotalDepositos(resumen.getTotalDepositos().add(monto));
                resumen.setSaldoAnual(resumen.getSaldoAnual().add(monto));
            } else {
                resumen.setTotalRetiros(resumen.getTotalRetiros().add(monto));
                resumen.setSaldoAnual(resumen.getSaldoAnual().subtract(monto));
            }
            resumen.setFecha(item.getFecha());
            resumen.setTransaccion(item.getTransaccion());
            resumen.setMonto(monto);
            resumen.setTipoRegistro(item.getTipoRegistro());
        }

        if (!resumenAnual.isEmpty()) {
            cuentaAnualRepository.saveAll(resumenAnual.values());
            System.out.println("[CuentaAnualWriter] Registros anuales consolidados: " + resumenAnual.size());
        }
    }
}