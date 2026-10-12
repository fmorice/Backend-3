package cl.duoc.eft.s9.clientes;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class TransferenciaConsumerTest {

    @Test
    void procesaEventoDeTransferencia() {
        TransferenciaConsumer consumer = new TransferenciaConsumer();
        TransferenciaEvent evento = new TransferenciaEvent();
        evento.setEventId("evt-001");
        evento.setTipoEvento("TRANSFERENCIA_REALIZADA");
        evento.setCuentaOrigen("123456");
        evento.setCuentaDestino("654321");
        evento.setMonto(250.50);
        evento.setMoneda("USD");
        evento.setDescripcion("Pago de servicio");
        evento.setTimestamp("2026-09-27T10:00:00");

        assertDoesNotThrow(() -> consumer.handleTransferencia(evento));
    }
}