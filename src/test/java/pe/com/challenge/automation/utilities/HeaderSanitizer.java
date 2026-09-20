package pe.com.challenge.automation.utilities;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class HeaderSanitizer {
    private HeaderSanitizer() {
    }

    public static Map<String, String> maskSecrets(Map<String, String> headers) {
        Map<String, String> safe = new LinkedHashMap<>();
        if (headers == null) {
            return safe;
        }
        headers.forEach((name, value) -> safe.put(name,
                isSensitive(name) ? masked(value) : value));
        return safe;
    }

    private static boolean isSensitive(String name) {
        String normalized = name == null ? "" : name.toLowerCase(Locale.ROOT);
        return normalized.contains("authorization")
                || normalized.contains("api-key")
                || normalized.contains("apikey")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("subscription-key")
                || normalized.contains("cookie");
    }

    private static String masked(String value) {
        if (value == null || value.isBlank()) {
            return "[EMPTY]";
        }
        int visible = Math.min(4, value.length());
        return "********" + value.substring(value.length() - visible);
    }
}
