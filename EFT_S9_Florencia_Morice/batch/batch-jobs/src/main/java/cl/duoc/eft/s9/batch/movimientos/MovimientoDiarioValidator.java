package cl.duoc.eft.s9.batch.movimientos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MovimientoDiarioValidator {

    private static final Set<String> TIPOS_VALIDOS = Set.of(
            "credito",
            "debito",
            "deposito",
            "retiro",
            "compra",
            "pago"
    );

    public ValidationResult validate(MovimientoDiarioCsv item) {
        List<String> reasons = new ArrayList<>();
        if (item == null) {
            return new ValidationResult(false, List.of("registro nulo"), false);
        }

        if (isBlank(item.getId())) {
            reasons.add("id obligatorio");
        }

        if (isBlank(item.getFecha())) {
            reasons.add("fecha obligatoria");
        } else if (!isValidDate(item.getFecha())) {
            reasons.add("fecha inválida o ambigua");
        }

        if (isBlank(item.getMonto())) {
            reasons.add("monto obligatorio");
        } else if (!isNumeric(item.getMonto())) {
            reasons.add("monto no numérico");
        }

        if (isBlank(item.getTipo())) {
            reasons.add("tipo obligatorio");
        } else if (!TIPOS_VALIDOS.contains(normalizeType(item.getTipo()))) {
            reasons.add("tipo de movimiento no reconocido");
        }

        boolean businessPending = false;
        if (reasons.isEmpty() && !isBlank(item.getMonto()) && isNumeric(item.getMonto())) {
            BigDecimal monto = new BigDecimal(item.getMonto().trim());
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                businessPending = true;
            }
        }

        return new ValidationResult(reasons.isEmpty(), reasons, businessPending);
    }

    public String normalizeType(String tipo) {
        if (tipo == null) {
            return "";
        }
        return tipo.trim().toLowerCase(Locale.ROOT)
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ü", "u")
                .replace("ñ", "n");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isNumeric(String value) {
        if (isBlank(value)) {
            return false;
        }
        try {
            new BigDecimal(value.trim());
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isValidDate(String value) {
        String candidate = value.trim();
        List<DateTimeFormatter> formatters = List.of(
            DateTimeFormatter.ofPattern("uuuu/MM/dd").withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT)
        );

        if (candidate.contains("/") || candidate.contains("-")) {
            if (candidate.indexOf('/') != -1) {
                String[] parts = candidate.split("/");
                if (parts.length == 3 && isAmbiguousDayMonth(parts)) {
                    return false;
                }
            }
            if (candidate.indexOf('-') != -1) {
                String[] parts = candidate.split("-");
                if (parts.length == 3 && isAmbiguousDayMonth(parts)) {
                    return false;
                }
            }
        }

        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDate.parse(candidate, formatter);
                return true;
            } catch (DateTimeParseException ignored) {
                // continuar; el patrón no coincide
            }
        }
        return false;
    }

    private boolean isAmbiguousDayMonth(String[] parts) {
        if (parts.length != 3) {
            return false;
        }
        try {
            int first = Integer.parseInt(parts[0]);
            int second = Integer.parseInt(parts[1]);
            return first >= 1 && first <= 12 && second >= 1 && second <= 12;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public record ValidationResult(boolean valid, List<String> reasons, boolean pendingBusinessDecision) {
    }
}
