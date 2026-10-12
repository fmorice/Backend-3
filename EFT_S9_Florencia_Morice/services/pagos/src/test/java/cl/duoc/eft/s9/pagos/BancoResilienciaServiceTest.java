package cl.duoc.eft.s9.pagos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.junit.jupiter.api.Test;

class BancoResilienciaServiceTest {

    @Test
    void declaresCircuitBreakerAndFallbackContract() throws Exception {
        CircuitBreaker annotation = BancoResilienciaService.class
                .getMethod("unstable", boolean.class)
                .getAnnotation(CircuitBreaker.class);
        BancoResilienciaService service = new BancoResilienciaService();

        assertEquals("bancoResiliencia", annotation.name());
        assertEquals("fallback", annotation.fallbackMethod());
        assertEquals("OK", service.unstable(false).get("estado"));
        assertTrue(service.fallback(true, new IllegalStateException()).containsKey("estado"));
    }
}