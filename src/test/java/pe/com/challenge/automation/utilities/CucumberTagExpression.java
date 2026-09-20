package pe.com.challenge.automation.utilities;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Evalúa la expresión configurada en cucumber.filter.tags para que el contador
 * en vivo considere únicamente los escenarios que Cucumber realmente ejecuta.
 */
public final class CucumberTagExpression {
    private CucumberTagExpression() {
    }

    public static boolean matches(String expression, Set<String> sourceTags) {
        if (expression == null || expression.isBlank()) {
            return true;
        }
        Set<String> normalizedTags = sourceTags.stream()
                .map(CucumberTagExpression::normalizeTag)
                .collect(Collectors.toUnmodifiableSet());
        try {
            Parser parser = new Parser(expression, normalizedTags);
            boolean result = parser.parseOr();
            parser.requireEnd();
            return result;
        } catch (IllegalArgumentException ignored) {
            // Cucumber reportará una expresión inválida; el listener no debe
            // ocultar ni interrumpir la causa original de la ejecución.
            return true;
        }
    }

    private static String normalizeTag(String tag) {
        String normalized = tag == null ? "" : tag.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("@") ? normalized : "@" + normalized;
    }

    private static final class Parser {
        private final String expression;
        private final Set<String> tags;
        private int index;
        private String lookahead;

        private Parser(String expression, Set<String> tags) {
            this.expression = expression;
            this.tags = tags;
            this.lookahead = nextToken();
        }

        private boolean parseOr() {
            boolean value = parseAnd();
            while (is("or")) {
                consume();
                boolean right = parseAnd();
                value = value || right;
            }
            return value;
        }

        private boolean parseAnd() {
            boolean value = parseUnary();
            while (is("and")) {
                consume();
                boolean right = parseUnary();
                value = value && right;
            }
            return value;
        }

        private boolean parseUnary() {
            if (is("not")) {
                consume();
                return !parseUnary();
            }
            return parsePrimary();
        }

        private boolean parsePrimary() {
            if (is("(")) {
                consume();
                boolean value = parseOr();
                require(")");
                consume();
                return value;
            }
            if (lookahead == null || is(")") || is("and") || is("or")) {
                throw new IllegalArgumentException("Expresión de tags incompleta");
            }
            String tag = normalizeTag(lookahead);
            consume();
            return tags.contains(tag);
        }

        private void requireEnd() {
            if (lookahead != null) {
                throw new IllegalArgumentException("Token inesperado: " + lookahead);
            }
        }

        private void require(String expected) {
            if (!is(expected)) {
                throw new IllegalArgumentException("Se esperaba " + expected);
            }
        }

        private boolean is(String expected) {
            return lookahead != null && lookahead.equalsIgnoreCase(expected);
        }

        private void consume() {
            lookahead = nextToken();
        }

        private String nextToken() {
            while (index < expression.length() && Character.isWhitespace(expression.charAt(index))) {
                index++;
            }
            if (index >= expression.length()) {
                return null;
            }
            char current = expression.charAt(index);
            if (current == '(' || current == ')') {
                index++;
                return String.valueOf(current);
            }
            int start = index;
            while (index < expression.length()) {
                current = expression.charAt(index);
                if (Character.isWhitespace(current) || current == '(' || current == ')') {
                    break;
                }
                index++;
            }
            return expression.substring(start, index);
        }
    }
}
