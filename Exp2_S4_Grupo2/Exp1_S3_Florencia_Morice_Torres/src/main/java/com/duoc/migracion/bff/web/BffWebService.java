package com.duoc.migracion.bff.web;

import com.duoc.migracion.dto.CuentaDto;
import com.duoc.migracion.service.CuentaService;
import org.springframework.stereotype.Service;

@Service
public class BffWebService {

    private final CuentaService cuentaService;

    public BffWebService(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    public CuentaDto obtenerCuentaCompleta(Long cuentaId) {
        return cuentaService.obtenerCuentaPorId(cuentaId);
    }
}
