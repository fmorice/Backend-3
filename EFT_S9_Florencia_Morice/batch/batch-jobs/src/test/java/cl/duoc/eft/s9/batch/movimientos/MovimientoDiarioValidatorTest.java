package cl.duoc.eft.s9.batch.movimientos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.validator.ValidationException;

class MovimientoDiarioValidatorTest {

    private final MovimientoDiarioValidator validator = new MovimientoDiarioValidator();

    @Test
    void reportsAllRequiredFieldsWhenValuesAreBlank() {
        var result = validator.validate(new MovimientoDiarioCsv(" ", "", "  ", ""));

        assertFalse(result.valid());
        assertFalse(result.pendingBusinessDecision());
        assertEquals(Set.of("id obligatorio", "fecha obligatoria", "monto obligatorio", "tipo obligatorio"),
                result.reasons().stream().collect(Collectors.toSet()));
    }

    @Test
    void acceptsSupportedDateFormatsAndRejectsInvalidOrAmbiguousDates() {
        assertTrue(validator.validate(movement("2024-04-15", "100", "credito")).valid());
        assertTrue(validator.validate(movement("15/04/2024", "100", "debito")).valid());
        assertFalse(validator.validate(movement("11-12-2024", "100", "credito")).valid());
        assertFalse(validator.validate(movement("31/02/2024", "100", "credito")).valid());
    }

    @Test
    void keepsNegativeAndZeroAmountsPendingWithoutRejectingThem() {
        var negative = validator.validate(movement("2024-04-15", "-200", "credito"));
        var zero = validator.validate(movement("2024-04-15", "0", "debito"));

        assertTrue(negative.valid());
        assertTrue(negative.pendingBusinessDecision());
        assertTrue(zero.valid());
        assertTrue(zero.pendingBusinessDecision());
    }

    @Test
    void doesNotMarkTechnicallyInvalidMovementAsBusinessPending() {
        var result = validator.validate(movement("2024-04-15", "-200", "tipo-desconocido"));

        assertFalse(result.valid());
        assertFalse(result.pendingBusinessDecision());
        assertTrue(result.reasons().contains("tipo de movimiento no reconocido"));
    }

    @Test
    void processorDistinguishesPendingFromTechnicallyValidUnclassifiedMovement() throws Exception {
        ItemProcessor<MovimientoDiarioCsv, MovimientoDiarioReporte> processor =
                new MovimientosBatchConfiguration().movimientosProcessor();

        MovimientoDiarioReporte pending = processor.process(movement("2024-04-15", "-200", "credito"));
        MovimientoDiarioReporte valid = processor.process(movement("2024-04-15", "200", "credito"));

        assertEquals("PENDIENTE_NEGOCIO", pending.getEstado());
        assertEquals("VALIDADO_TECNICAMENTE", valid.getEstado());
    }

    @Test
    void processorRejectsUnknownMovementType() {
        ItemProcessor<MovimientoDiarioCsv, MovimientoDiarioReporte> processor =
                new MovimientosBatchConfiguration().movimientosProcessor();

        org.junit.jupiter.api.Assertions.assertThrows(ValidationException.class,
                () -> processor.process(movement("2024-04-15", "200", "tipo-desconocido")));
    }

    private MovimientoDiarioCsv movement(String date, String amount, String type) {
        return new MovimientoDiarioCsv("id-1", date, amount, type);
    }
}