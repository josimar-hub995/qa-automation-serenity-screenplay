package pe.com.challenge.automation.utilities;

import pe.com.challenge.automation.exceptions.FrameworkException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DirectoryUtility {
    private DirectoryUtility() {
    }

    public static int nextExecutionNumber(Path root, String prefix) {
        if (!Files.exists(root)) {
            return 1;
        }
        Pattern pattern = Pattern.compile("^" + Pattern.quote(prefix) + "_(\\d{3})_.*$");
        try (var paths = Files.list(root)) {
            return paths.filter(Files::isDirectory)
                    .map(path -> pattern.matcher(path.getFileName().toString()))
                    .filter(Matcher::matches)
                    .mapToInt(matcher -> Integer.parseInt(matcher.group(1)))
                    .max()
                    .orElse(0) + 1;
        } catch (IOException e) {
            throw new FrameworkException("No se pudo calcular el número de reporte en " + root, e);
        }
    }
}
