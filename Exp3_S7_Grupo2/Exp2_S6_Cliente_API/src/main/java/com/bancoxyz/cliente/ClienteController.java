package com.bancoxyz.cliente;
import com.bancoxyz.cliente.model.Cliente;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    @Value("${example.message:Valor no cargado}")
    private String configMessage;
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of("estado", "OK", "servicio", "CLIENTE-XYZ", "configMessage", configMessage));
    }
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> getCliente(@PathVariable Long id) {
        Cliente c = new Cliente(id, "12.345.678-9", id == 1L ? "Juan Perez" : "Maria Gonzalez", "cliente" + id + "@bancoxyz.cl", "Preferencial");
        return ResponseEntity.ok(c);
    }
}
