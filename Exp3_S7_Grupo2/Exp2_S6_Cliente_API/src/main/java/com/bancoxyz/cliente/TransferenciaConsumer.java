package com.bancoxyz.cliente;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransferenciaConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferenciaConsumer.class);

    @KafkaListener(topics = "banco-xyz.transferencias", groupId = "cliente-xyz-group")
    public void consume(TransferenciaEvent evento) {
        if (evento == null) {
            log.warn("Se recibió un evento nulo en banco-xyz.transferencias");
            return;
        }

        if ("TRANSFERENCIA_REALIZADA".equalsIgnoreCase(evento.getTipoEvento())) {
            log.info("Evento Kafka recibido: eventId={}, tipoEvento={}, cuentaOrigen={}, cuentaDestino={}, monto={}, moneda={}, descripcion={}, timestamp={}",
                    evento.getEventId(),
                    evento.getTipoEvento(),
                    evento.getCuentaOrigen(),
                    evento.getCuentaDestino(),
                    evento.getMonto(),
                    evento.getMoneda(),
                    evento.getDescripcion(),
                    evento.getTimestamp());
        } else {
            log.info("Evento recibido ignorado por tipo no soportado: tipoEvento={}, eventId={}", evento.getTipoEvento(), evento.getEventId());
        }
    }

    public void handleTransferencia(TransferenciaEvent evento) {
        consume(evento);
    }
}
