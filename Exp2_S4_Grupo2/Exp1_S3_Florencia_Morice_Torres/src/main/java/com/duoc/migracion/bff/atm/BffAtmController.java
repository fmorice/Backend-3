package com.duoc.migracion.bff.atm;

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
@RequestMapping("/api/bff/atm")
public class BffAtmController {

    private final BffAtmService bffAtmService;

    public BffAtmController(BffAtmService bffAtmService) {
        this.bffAtmService = bffAtmService;
    }

    @GetMapping("/cuentas/{cuentaId}/saldo")
    public ResponseEntity<SaldoAtmDto> obtenerSaldoCuenta(@PathVariable Long cuentaId) {
        return ResponseEntity.ok(bffAtmService.obtenerSaldoCuenta(cuentaId));
    }

    @ExceptionHandler(CuentaNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> manejarCuentaNoEncontrada(CuentaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", ex.getMessage()));
    }
}
