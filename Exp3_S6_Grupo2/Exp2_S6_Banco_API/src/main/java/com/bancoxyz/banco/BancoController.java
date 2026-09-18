package com.bancoxyz.banco;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;

@RestController
@RequestMapping("/api/banco")
public class BancoController {

    @Value("${example.message:Mensaje por defecto}")
    private String exampleMessage;

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "Banco API running");
    }

    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of("message", exampleMessage);
    }

    @Autowired
    private BancoResilienciaService resilienciaService;

    @GetMapping("/seguro")
    public Map<String, String> seguro() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuario = (auth != null) ? auth.getName() : "anonymous";
        return Map.of("mensaje", "Acceso autorizado", "usuario", usuario);
    }

    @GetMapping("/resiliencia")
    public Map<String, String> resiliencia(@RequestParam(name = "fail", required = false, defaultValue = "false") boolean fail) {
        return resilienciaService.unstable(fail);
    }

}
