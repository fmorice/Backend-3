package com.duoc.migracion.service;

import com.duoc.migracion.dto.CuentaDto;
import com.duoc.migracion.exception.AccountNotFoundException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class AccountService {

    private final Map<Long, CuentaDto> cuentas = new HashMap<>();

    @PostConstruct
    public void loadData() {
        try {
            var res = new ClassPathResource("data/intereses.csv");
            try (var is = res.getInputStream();
                 var br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line = br.readLine(); // header
                while ((line = br.readLine()) != null) {
                    if (line.isBlank()) continue;
                    var parts = line.split(",");
                    if (parts.length < 5) continue;
                    Long id = Long.parseLong(parts[0].trim());
                    String nombre = parts[1].trim();
                    Double saldo = Double.parseDouble(parts[2].trim());
                    Integer edad = Integer.parseInt(parts[3].trim());
                    String tipo = parts[4].trim();
                    CuentaDto dto = new CuentaDto(id, nombre, saldo, edad, tipo);
                    cuentas.put(id, dto);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error cargando datos de cuentas: " + e.getMessage(), e);
        }
    }

    public CuentaDto getById(Long id) {
        CuentaDto dto = cuentas.get(id);
        if (dto == null) throw new AccountNotFoundException(id);
        return dto;
    }
}
