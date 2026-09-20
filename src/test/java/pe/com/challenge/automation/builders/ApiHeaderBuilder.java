package pe.com.challenge.automation.builders;

import pe.com.challenge.automation.configurations.HeaderErrorConfiguration;
import pe.com.challenge.automation.configurations.HeaderErrorConfiguration.Mutation;
import pe.com.challenge.automation.exceptions.TestDataException;
import pe.com.challenge.automation.managers.ConfigurationManager;
import pe.com.challenge.automation.utilities.JsonUtility;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Construye todos los headers desde la columna HEADERS del Excel. */
public final class ApiHeaderBuilder {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([A-Za-z0-9_.-]+)}");

    private ApiHeaderBuilder() {
    }

    public static Map<String, String> build(String headersJson, String errorConfiguration) {
        Map<String, Object> source = JsonUtility.readObject(headersJson, "HEADERS");
        Map<String, String> headers = new LinkedHashMap<>();
        source.forEach((name, value) -> {
            String headerName = name == null ? "" : name.trim();
            if (headerName.isBlank()) {
                throw new TestDataException("HEADERS contiene un nombre de header vacío");
            }
            headers.put(headerName, resolve(String.valueOf(value)));
        });

        for (Mutation mutation : HeaderErrorConfiguration.parse(errorConfiguration)) {
            String actualName = findHeaderName(headers, mutation.headerName());
            if (actualName == null) {
                throw new TestDataException("HEADER_ERROR_CONFIG intenta modificar '"
                        + mutation.headerName() + "', pero no existe en la columna HEADERS");
            }
            switch (mutation.mode()) {
                case OMIT -> headers.remove(actualName);
                case EMPTY -> headers.put(actualName, "");
                case INCORRECT -> headers.put(actualName, resolve(mutation.replacementValue()));
            }
        }
        return Map.copyOf(headers);
    }

    private static String findHeaderName(Map<String, String> headers, String expected) {
        return headers.keySet().stream()
                .filter(name -> name.equalsIgnoreCase(expected))
                .findFirst()
                .orElse(null);
    }

    private static String resolve(String value) {
        Matcher matcher = PLACEHOLDER.matcher(value == null ? "" : value);
        StringBuffer resolved = new StringBuffer();
        while (matcher.find()) {
            String configuredValue = ConfigurationManager.required(matcher.group(1));
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(configuredValue));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }
}
