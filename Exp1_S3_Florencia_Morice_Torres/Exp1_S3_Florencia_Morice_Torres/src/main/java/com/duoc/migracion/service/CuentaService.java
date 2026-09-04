package com.duoc.migracion.service;

import com.duoc.migracion.dto.CuentaDto;
import com.duoc.migracion.exception.CuentaNoEncontradaException;
import com.duoc.migracion.model.Interes;
import com.duoc.migracion.repository.InteresRepository;
import org.springframework.stereotype.Service;

@Service
public class CuentaService {

    private final InteresRepository interesRepository;

    public CuentaService(InteresRepository interesRepository) {
        this.interesRepository = interesRepository;
    }

    public CuentaDto obtenerCuentaPorId(Long cuentaId) {
        Interes interes = interesRepository.findById(cuentaId)
                .orElseThrow(() -> new CuentaNoEncontradaException("Cuenta no encontrada"));

        return new CuentaDto(
                interes.getCuentaId(),
                interes.getNombre(),
                interes.getSaldo() != null ? interes.getSaldo().doubleValue() : null,
                interes.getEdad(),
                interes.getTipo()
        );
    }
}
