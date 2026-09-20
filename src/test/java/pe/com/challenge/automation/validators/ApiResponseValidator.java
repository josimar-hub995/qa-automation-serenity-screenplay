package pe.com.challenge.automation.validators;

import com.fasterxml.jackson.databind.JsonNode;
import pe.com.challenge.automation.models.ApiTestData;
import pe.com.challenge.automation.utilities.JsonUtility;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class ApiResponseValidator {
    private ApiResponseValidator() {
    }

    public static void validate(ApiTestData data, int actualStatus, String actualBody) {
        List<String> errors = new ArrayList<>();
        if (actualStatus != data.expectedStatus()) {
            errors.add("Status esperado " + data.expectedStatus() + ", obtenido " + actualStatus);
        }

        JsonNode expected = JsonUtility.readTree(data.expectedResponse(), "RESPUESTA_ESPERADA");
        JsonNode actual = JsonUtility.readTree(actualBody, "response body");
        compare(expected, actual, "$", errors);

        if (!errors.isEmpty()) {
            throw new AssertionError("Validación API fallida para los datos " + data.id()
                    + ":" + System.lineSeparator() + " - "
                    + String.join(System.lineSeparator() + " - ", errors));
        }
    }

    private static void compare(JsonNode expected, JsonNode actual, String path, List<String> errors) {
        if (expected.isTextual() && expected.asText().startsWith("$")) {
            validateToken(expected.asText(), actual, path, errors);
            return;
        }
        if (expected.isObject()) {
            if (!actual.isObject()) {
                errors.add(path + " debía ser un objeto JSON");
                return;
            }
            Iterator<Map.Entry<String, JsonNode>> fields = expected.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (!actual.has(field.getKey())) {
                    errors.add(path + "." + field.getKey() + " no existe en el response");
                } else {
                    compare(field.getValue(), actual.get(field.getKey()),
                            path + "." + field.getKey(), errors);
                }
            }
            return;
        }
        if (expected.isArray()) {
            if (!actual.isArray()) {
                errors.add(path + " debía ser un arreglo JSON");
                return;
            }
            if (expected.size() > actual.size()) {
                errors.add(path + " contiene menos elementos de los esperados");
                return;
            }
            for (int index = 0; index < expected.size(); index++) {
                compare(expected.get(index), actual.get(index), path + "[" + index + "]", errors);
            }
            return;
        }
        if (!expected.equals(actual)) {
            errors.add(path + " esperado=" + expected + ", obtenido=" + actual);
        }
    }

    private static void validateToken(String token, JsonNode actual, String path, List<String> errors) {
        boolean valid = switch (token) {
            case "$present", "$any" -> actual != null && !actual.isMissingNode() && !actual.isNull();
            case "$notEmpty" -> notEmpty(actual);
            case "$string" -> actual != null && actual.isTextual();
            case "$number" -> actual != null && actual.isNumber();
            case "$boolean" -> actual != null && actual.isBoolean();
            default -> false;
        };
        if (!valid) {
            errors.add(path + " no cumple el matcher " + token);
        }
    }

    private static boolean notEmpty(JsonNode value) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return false;
        }
        if (value.isTextual()) {
            return !value.asText().isBlank();
        }
        if (value.isArray() || value.isObject()) {
            return !value.isEmpty();
        }
        return true;
    }
}
