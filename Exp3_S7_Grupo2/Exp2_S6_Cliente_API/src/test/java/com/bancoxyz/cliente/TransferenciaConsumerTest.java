package com.bancoxyz.cliente;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TransferenciaConsumerTest {

    @Test
    void procesaEventoTransferenciaRealizada() {
        TransferenciaConsumer consumer = new TransferenciaConsumer();
        TransferenciaEvent evento = new TransferenciaEvent(
                "evt-001",
                "TRANSFERENCIA_REALIZADA",
                "123456",
                "654321",
                250.50,
                "USD",
                "Pago de servicio",
                "2026-09-27T10:00:00"
        );

        assertDoesNotThrow(() -> consumer.handleTransferencia(evento));
    }
}
