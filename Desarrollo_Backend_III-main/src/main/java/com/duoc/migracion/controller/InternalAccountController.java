package com.duoc.migracion.controller;

import com.duoc.migracion.dto.CuentaDto;
import com.duoc.migracion.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/cuentas")
public class InternalAccountController {

    private final AccountService accountService;

    public InternalAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    public CuentaDto get(@PathVariable Long id) {
        return accountService.getById(id);
    }
}
