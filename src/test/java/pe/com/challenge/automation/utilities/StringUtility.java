package pe.com.challenge.automation.utilities;

import java.text.Normalizer;
import java.util.Locale;

public final class StringUtility {
    private StringUtility() {
    }

    public static String safeFileName(String value) {
        String normalized = Normalizer.normalize(value == null ? "sin_nombre" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9_-]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        return normalized.isBlank() ? "sin_nombre" : normalized;
    }

    public static boolean isTrue(String value) {
        String normalized = normalizeForComparison(value);
        return normalized.equals("si")
                || normalized.equals("yes")
                || normalized.equals("true")
                || normalized.equals("1");
    }

    public static String normalizeForComparison(String value) {
        return Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ")
                .trim();
    }
}
