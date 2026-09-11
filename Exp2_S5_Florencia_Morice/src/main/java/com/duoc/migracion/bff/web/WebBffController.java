package com.duoc.migracion.bff.web;

import com.duoc.migracion.bff.web.dto.WebCuentaResponse;
import com.duoc.migracion.dto.CuentaDto;
import com.duoc.migracion.service.AccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bff/web/cuentas")
public class WebBffController {

    private final AccountService accountService;

    public WebBffController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('WEB')")
    public WebCuentaResponse getWebCuenta(@PathVariable Long id) {
        CuentaDto c = accountService.getById(id);
        String estado = c.saldo() >= 0 ? "ACTIVA" : "EN_DEUDA";
        return new WebCuentaResponse(c.cuentaId(), c.nombre(), c.saldo(), c.edad(), c.tipo(), estado);
    }
}
