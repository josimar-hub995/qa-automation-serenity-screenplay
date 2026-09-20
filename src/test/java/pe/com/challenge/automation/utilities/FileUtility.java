package pe.com.challenge.automation.utilities;

import pe.com.challenge.automation.exceptions.FrameworkException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class FileUtility {
    private FileUtility() {
    }

    public static Path createDirectories(Path path) {
        try {
            return Files.createDirectories(path);
        } catch (IOException e) {
            throw new FrameworkException("No se pudo crear el directorio: " + path, e);
        }
    }

    public static void writeText(Path path, String content) {
        try {
            createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FrameworkException("No se pudo escribir el archivo: " + path, e);
        }
    }

    public static void appendText(Path path, String content) {
        try {
            createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8,
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new FrameworkException("No se pudo actualizar el archivo: " + path, e);
        }
    }

    public static void copy(Path source, Path target) {
        try {
            createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FrameworkException("No se pudo copiar " + source + " a " + target, e);
        }
    }
}
