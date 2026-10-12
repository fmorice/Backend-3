package cl.duoc.eft.s9.pagos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.Test;

class BancoControllerTest {

    @Test
    void publicaTransferenciaConElContratoRecuperado() {
        BancoResilienciaService resilienciaService = new BancoResilienciaService();
        RecordingTransferenciaProducer producer = new RecordingTransferenciaProducer();
        BancoController controller = new BancoController(resilienciaService, producer);
        TransferenciaRequest request = new TransferenciaRequest();
        request.setCuentaOrigen("123456");
        request.setCuentaDestino("654321");
        request.setMonto(250.50);
        request.setMoneda("USD");
        request.setDescripcion("Pago de servicio");

        Map<String, Object> response = controller.registrarTransferencia(request);

        assertEquals("TRANSFERENCIA_REALIZADA", producer.event.getTipoEvento());
        assertNotNull(producer.event.getEventId());
        assertEquals("OK", response.get("status"));
        assertEquals("123456", response.get("cuentaOrigen"));
    }

    private static class RecordingTransferenciaProducer extends TransferenciaProducer {
        private TransferenciaEvent event;

        private RecordingTransferenciaProducer() {
            super(null);
        }

        @Override
        public void sendTransferencia(TransferenciaEvent evento) {
            event = evento;
        }
    }
}