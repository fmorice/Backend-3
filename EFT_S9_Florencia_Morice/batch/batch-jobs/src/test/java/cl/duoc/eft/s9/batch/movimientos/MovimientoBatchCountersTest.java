package cl.duoc.eft.s9.batch.movimientos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;

class MovimientoBatchCountersTest {

    @Test
    void reconcilesTheExistingThousandRecordRunWithoutDoubleCountingPendingRecords() {
        MovimientoBatchCounters counters = MovimientoBatchCounters.from(1000, 0, 391, 609, 0, 71);

        assertEquals(1000, counters.registrosLeidos());
        assertEquals(391, counters.registrosEscritos());
        assertEquals(71, counters.registrosPendientesNegocio());
        assertEquals(0, counters.registrosAceptadosNegocio());
        assertEquals(320, counters.registrosSinDecisionNegocio());
        assertEquals(609, counters.registrosRechazados());
        assertTrue(counters.estaConciliado());
        assertEquals(counters.registrosEscritos(), counters.registrosPendientesNegocio()
                + counters.registrosAceptadosNegocio() + counters.registrosSinDecisionNegocio());
    }

    @Test
    void includesReadAndWriteErrorsInReconciliationSeparatelyFromTechnicalRejections() {
        MovimientoBatchCounters counters = MovimientoBatchCounters.from(10, 1, 7, 2, 1, 2);

        assertEquals(11, counters.registrosLeidos());
        assertEquals(2, counters.registrosRechazados());
        assertEquals(1, counters.erroresLectura());
        assertEquals(1, counters.erroresEscritura());
        assertTrue(counters.estaConciliado());
    }

    @Test
    void rejectsCountersThatWouldExceedTheirParentCategory() {
        assertThrows(IllegalArgumentException.class, () -> MovimientoBatchCounters.from(1, 0, 1, 0, 0, 2));
        assertFalse(MovimientoBatchCounters.from(2, 0, 1, 0, 0, 0).estaConciliado());
    }

    @Test
    void recordsSkippedReadAndWriteErrorsWithAvailableRowData() {
        var tracker = new MovimientosBatchConfiguration.RejectedMovimientoTracker();
        SkipListener<MovimientoDiarioCsv, MovimientoDiarioReporte> listener =
                new MovimientosBatchConfiguration().movimientosSkipListener(tracker);
        MovimientoDiarioReporte report = new MovimientoDiarioReporte("id-2", "2024-04-15", "80", "pago", "VALIDADO_TECNICAMENTE");
        report.setNumeroFila(9);

        listener.onSkipInRead(new FlatFileParseException("fila ilegible", "dato,incompleto", 7));
        listener.onSkipInWrite(report, new IOException("disco no disponible"));

        assertEquals(2, tracker.getRejected().size());
        assertEquals(7, tracker.getRejected().get(0).getNumeroFila());
        assertTrue(tracker.getRejected().get(0).getMotivo().startsWith("error_lectura:"));
        assertEquals(9, tracker.getRejected().get(1).getNumeroFila());
        assertTrue(tracker.getRejected().get(1).getMotivo().startsWith("error_escritura:"));
    }
}