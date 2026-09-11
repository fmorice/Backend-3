package com.duoc.migracion.bff.cajero;

import com.duoc.migracion.bff.cajero.dto.CajeroCuentaResponse;
import com.duoc.migracion.dto.CuentaDto;
import com.duoc.migracion.service.AccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bff/cajero/cuentas")
public class CajeroBffController {

    private final AccountService accountService;

    public CajeroBffController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CAJERO')")
    public CajeroCuentaResponse getCajeroCuenta(@PathVariable Long id) {
        CuentaDto c = accountService.getById(id);
        return new CajeroCuentaResponse(c.cuentaId(), c.saldo());
    }
}
