package pe.com.challenge.automation.managers;

import pe.com.challenge.automation.exceptions.FrameworkException;
import pe.com.challenge.automation.utilities.ConfigUtility;

import java.util.Locale;
import java.util.Properties;

public final class ConfigurationManager {
    private static final String ENVIRONMENT = System.getProperty("environment", "qa");
    private static final Properties ENVIRONMENT_PROPERTIES = ConfigUtility.load("config/" + ENVIRONMENT + ".properties");
    private static final Properties BROWSER_PROPERTIES = ConfigUtility.load("config/browser.properties");
    private static final Properties TEST_PROPERTIES = ConfigUtility.load("config/test.properties");

    private ConfigurationManager() {
    }

    public static String get(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }
        String environmentValue = System.getenv(toEnvironmentKey(key));
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue;
        }
        if (ENVIRONMENT_PROPERTIES.containsKey(key)) {
            return ENVIRONMENT_PROPERTIES.getProperty(key);
        }
        if (BROWSER_PROPERTIES.containsKey(key)) {
            return BROWSER_PROPERTIES.getProperty(key);
        }
        return TEST_PROPERTIES.getProperty(key);
    }

    public static String required(String key) {
        String value = get(key);
        if (value == null || value.isBlank()) {
            throw new FrameworkException("Falta la configuración obligatoria: " + key
                    + " (propiedad -D" + key + " o variable " + toEnvironmentKey(key) + ")");
        }
        return value;
    }

    public static String environment() {
        return ENVIRONMENT_PROPERTIES.getProperty("environment", ENVIRONMENT.toUpperCase());
    }

    private static String toEnvironmentKey(String key) {
        return key.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "_");
    }
}
