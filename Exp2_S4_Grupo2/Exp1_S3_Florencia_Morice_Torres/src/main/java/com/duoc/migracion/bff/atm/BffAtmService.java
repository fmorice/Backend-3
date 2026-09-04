package com.duoc.migracion.bff.atm;

import com.duoc.migracion.dto.CuentaDto;
import com.duoc.migracion.service.CuentaService;
import org.springframework.stereotype.Service;

@Service
public class BffAtmService {

    private final CuentaService cuentaService;

    public BffAtmService(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    public SaldoAtmDto obtenerSaldoCuenta(Long cuentaId) {
        CuentaDto cuenta = cuentaService.obtenerCuentaPorId(cuentaId);
        return new SaldoAtmDto(cuenta.cuentaId(), cuenta.saldo());
    }
}
