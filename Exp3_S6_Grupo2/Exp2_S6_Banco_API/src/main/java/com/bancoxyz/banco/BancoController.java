package com.bancoxyz.banco;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

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

    @Autowired
    private TransferenciaProducer transferenciaProducer;

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

    @PostMapping("/transferencias")
    public Map<String, Object> registrarTransferencia(@RequestBody TransferenciaRequest request) {
        TransferenciaEvent evento = new TransferenciaEvent();
        evento.setEventId(UUID.randomUUID().toString());
        evento.setTipoEvento("TRANSFERENCIA_REALIZADA");
        evento.setCuentaOrigen(request.getCuentaOrigen());
        evento.setCuentaDestino(request.getCuentaDestino());
        evento.setMonto(request.getMonto());
        evento.setMoneda(request.getMoneda());
        evento.setDescripcion(request.getDescripcion());
        evento.setTimestamp(Instant.now().toString());

        transferenciaProducer.sendTransferencia(evento);

        return Map.of(
                "status", "OK",
                "mensaje", "Transferencia publicada en Kafka",
                "eventId", evento.getEventId(),
                "tipoEvento", evento.getTipoEvento(),
                "cuentaOrigen", evento.getCuentaOrigen(),
                "cuentaDestino", evento.getCuentaDestino(),
                "monto", evento.getMonto(),
                "moneda", evento.getMoneda()
        );
    }

}
