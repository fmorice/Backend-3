package com.duoc.migracion.bff.mobile;

import com.duoc.migracion.dto.CuentaDto;
import com.duoc.migracion.service.CuentaService;
import org.springframework.stereotype.Service;

@Service
public class BffMobileService {

    private final CuentaService cuentaService;

    public BffMobileService(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    public CuentaMobileDto obtenerCuentaEsencial(Long cuentaId) {
        CuentaDto cuenta = cuentaService.obtenerCuentaPorId(cuentaId);
        return new CuentaMobileDto(
                cuenta.cuentaId(),
                cuenta.nombre(),
                cuenta.saldo(),
                cuenta.tipo()
        );
    }
}
