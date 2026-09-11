package com.duoc.migracion.bff.mobile;

import com.duoc.migracion.bff.mobile.dto.MobileCuentaResponse;
import com.duoc.migracion.dto.CuentaDto;
import com.duoc.migracion.service.AccountService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bff/mobile/cuentas")
public class MobileBffController {

    private final AccountService accountService;

    public MobileBffController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MOBILE')")
    public MobileCuentaResponse getMobileCuenta(@PathVariable Long id) {
        CuentaDto c = accountService.getById(id);
        return new MobileCuentaResponse(c.cuentaId(), c.nombre(), c.saldo(), c.tipo());
    }
}
