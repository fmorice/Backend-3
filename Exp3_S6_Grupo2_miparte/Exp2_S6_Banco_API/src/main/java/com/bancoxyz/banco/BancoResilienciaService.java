package com.bancoxyz.banco;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BancoResilienciaService {

    @CircuitBreaker(name = "bancoResiliencia", fallbackMethod = "fallback")
    public Map<String, String> unstable(boolean fail) {
        if (fail) {
            throw new RuntimeException("Fallo forzado para probar fallback");
        }
        return Map.of("estado", "OK", "mensaje", "Servicio respondió correctamente");
    }

    public Map<String, String> fallback(boolean fail, Throwable t) {
        return Map.of(
                "estado", "FALLBACK",
                "mensaje", "Servicio temporalmente no disponible. Se ejecutó el mecanismo de respaldo."
        );
    }

}
