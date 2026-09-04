package com.duoc.migracion.bff.mobile;

import com.duoc.migracion.exception.CuentaNoEncontradaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/bff/mobile")
public class BffMobileController {

    private final BffMobileService bffMobileService;

    public BffMobileController(BffMobileService bffMobileService) {
        this.bffMobileService = bffMobileService;
    }

    @GetMapping("/cuentas/{cuentaId}")
    public ResponseEntity<CuentaMobileDto> obtenerCuentaMobile(@PathVariable Long cuentaId) {
        return ResponseEntity.ok(bffMobileService.obtenerCuentaEsencial(cuentaId));
    }

    @ExceptionHandler(CuentaNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> manejarCuentaNoEncontrada(CuentaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", ex.getMessage()));
    }
}
