package pe.com.challenge.automation.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/**
 * Resuelve las opciones de ejecución desde una única fuente de configuración.
 * El orden de prioridad es: propiedad -D, variable de entorno y
 * serenity.properties.
 */
public final class ExecutionSettings {
    public static final String LOGS_PROPERTY = "execution.logs";
    public static final String LOGS_ENVIRONMENT = "EXECUTION_LOGS";
    private static final String SERENITY_PROPERTIES = "serenity.properties";

    private ExecutionSettings() {
    }

    public static boolean detailedLogsEnabled() {
        String systemValue = System.getProperty(LOGS_PROPERTY);
        if (hasText(systemValue)) {
            return enabled(systemValue);
        }

        String environmentValue = System.getenv(LOGS_ENVIRONMENT);
        if (hasText(environmentValue)) {
            return enabled(environmentValue);
        }

        return enabled(readProjectProperty(LOGS_PROPERTY, "false"));
    }

    private static String readProjectProperty(String key, String defaultValue) {
        Properties properties = new Properties();
        Path projectFile = Path.of(SERENITY_PROPERTIES).toAbsolutePath().normalize();

        if (Files.isRegularFile(projectFile)) {
            try (InputStream input = Files.newInputStream(projectFile)) {
                properties.load(input);
                return properties.getProperty(key, defaultValue);
            } catch (IOException ignored) {
                return defaultValue;
            }
        }

        try (InputStream input = ExecutionSettings.class.getClassLoader()
                .getResourceAsStream(SERENITY_PROPERTIES)) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException ignored) {
            return defaultValue;
        }
        return properties.getProperty(key, defaultValue);
    }

    private static boolean enabled(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("true")
                || normalized.equals("1")
                || normalized.equals("yes")
                || normalized.equals("si")
                || normalized.equals("sí");
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
