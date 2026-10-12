package cl.duoc.eft.s9.pagos;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/banco")
public class BancoController {

    private final BancoResilienciaService resilienciaService;
    private final TransferenciaProducer transferenciaProducer;

    @Value("${example.message:Mensaje por defecto}")
    private String exampleMessage;

    public BancoController(BancoResilienciaService resilienciaService, TransferenciaProducer transferenciaProducer) {
        this.resilienciaService = resilienciaService;
        this.transferenciaProducer = transferenciaProducer;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "Banco API running");
    }

    @GetMapping("/config")
    public Map<String, String> config() {
        return Map.of("message", exampleMessage);
    }

    @GetMapping("/seguro")
    public Map<String, String> seguro(org.springframework.security.core.Authentication authentication) {
        String usuario = authentication == null ? "anonymous" : authentication.getName();
        return Map.of("mensaje", "Acceso autorizado", "usuario", usuario);
    }

    @GetMapping("/resiliencia")
    public Map<String, String> resiliencia(
            @RequestParam(name = "fail", required = false, defaultValue = "false") boolean fail) {
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

        return Map.of("status", "OK", "mensaje", "Transferencia publicada en Kafka",
                "eventId", evento.getEventId(), "tipoEvento", evento.getTipoEvento(),
                "cuentaOrigen", evento.getCuentaOrigen(), "cuentaDestino", evento.getCuentaDestino(),
                "monto", evento.getMonto(), "moneda", evento.getMoneda());
    }
}