package pe.com.challenge.automation.configurations;

import pe.com.challenge.automation.exceptions.TestDataException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Interpreta las mutaciones declaradas en HEADER_ERROR_CONFIG.
 * Formatos admitidos (separados por punto y coma):
 * OMIT:nombre, EMPTY:nombre e INCORRECT:nombre=valor_incorrecto.
 */
public final class HeaderErrorConfiguration {
    private HeaderErrorConfiguration() {
    }

    public static List<Mutation> parse(String rawConfiguration) {
        if (rawConfiguration == null
                || rawConfiguration.isBlank()
                || "N/A".equalsIgnoreCase(rawConfiguration.trim())
                || "NONE".equalsIgnoreCase(rawConfiguration.trim())) {
            return List.of();
        }

        List<Mutation> mutations = new ArrayList<>();
        for (String token : rawConfiguration.split("[;\\r\\n]+")) {
            String expression = token.trim();
            if (expression.isBlank()) {
                continue;
            }
            int separator = expression.indexOf(':');
            if (separator <= 0 || separator == expression.length() - 1) {
                throw invalid(expression,
                        "use MODE:header o INCORRECT:header=valor");
            }

            Mode mode;
            try {
                mode = Mode.valueOf(expression.substring(0, separator)
                        .trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException error) {
                throw invalid(expression, "los modos permitidos son OMIT, EMPTY e INCORRECT");
            }

            String assignment = expression.substring(separator + 1).trim();
            int valueSeparator = assignment.indexOf('=');
            String headerName = (valueSeparator < 0 ? assignment : assignment.substring(0, valueSeparator)).trim();
            String replacement = valueSeparator < 0 ? "" : assignment.substring(valueSeparator + 1).trim();

            if (headerName.isBlank()) {
                throw invalid(expression, "debe indicar el nombre del header");
            }
            if (mode == Mode.INCORRECT && replacement.isBlank()) {
                throw invalid(expression,
                        "INCORRECT requiere el valor de prueba en el Excel: INCORRECT:header=valor");
            }
            if (mode != Mode.INCORRECT && valueSeparator >= 0) {
                throw invalid(expression, mode + " no admite un valor de reemplazo");
            }
            mutations.add(new Mutation(mode, headerName, replacement));
        }
        return List.copyOf(mutations);
    }

    private static TestDataException invalid(String expression, String detail) {
        return new TestDataException("HEADER_ERROR_CONFIG inválido ('" + expression + "'): " + detail);
    }

    public enum Mode {
        OMIT,
        EMPTY,
        INCORRECT
    }

    public record Mutation(Mode mode, String headerName, String replacementValue) {
    }
}
