package pe.com.challenge.automation.utilities;

import pe.com.challenge.automation.exceptions.FrameworkException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ResourceUtility {
    private ResourceUtility() {
    }

    public static Path copyTo(String resourcePath, Path target) {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new FrameworkException("No existe el recurso: " + resourcePath);
            }
            FileUtility.createDirectories(target.getParent());
            Files.copy(input, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return target;
        } catch (IOException e) {
            throw new FrameworkException("No se pudo copiar el recurso: " + resourcePath, e);
        }
    }
}
