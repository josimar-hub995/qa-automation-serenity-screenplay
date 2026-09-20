package pe.com.challenge.automation.utilities;

import pe.com.challenge.automation.exceptions.FrameworkException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigUtility {
    private ConfigUtility() {
    }

    public static Properties load(String resourcePath) {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new FrameworkException("No existe el archivo de configuración: " + resourcePath);
            }
            Properties properties = new Properties();
            properties.load(input);
            return properties;
        } catch (IOException e) {
            throw new FrameworkException("No se pudo leer la configuración: " + resourcePath, e);
        }
    }
}
