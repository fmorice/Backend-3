package com.duoc.migracion.bff.web;

import com.duoc.migracion.dto.CuentaDto;
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
@RequestMapping("/api/bff/web")
public class BffWebController {

    private final BffWebService bffWebService;

    public BffWebController(BffWebService bffWebService) {
        this.bffWebService = bffWebService;
    }

    @GetMapping("/cuentas/{cuentaId}")
    public ResponseEntity<CuentaDto> obtenerCuentaWeb(@PathVariable Long cuentaId) {
        return ResponseEntity.ok(bffWebService.obtenerCuentaCompleta(cuentaId));
    }

    @ExceptionHandler(CuentaNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> manejarCuentaNoEncontrada(CuentaNoEncontradaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", ex.getMessage()));
    }
}
