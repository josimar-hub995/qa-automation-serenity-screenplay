package pe.com.challenge.automation.utilities;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;

import java.nio.file.Path;
import java.util.stream.Collectors;

public final class BrowserConsoleUtility {
    private BrowserConsoleUtility() {
    }

    public static void save(WebDriver driver, Path target) {
        try {
            LogEntries entries = driver.manage().logs().get(LogType.BROWSER);
            String content = entries.getAll().stream()
                    .map(entry -> "%s %s %s".formatted(entry.getTimestamp(), entry.getLevel(), entry.getMessage()))
                    .collect(Collectors.joining(System.lineSeparator()));
            FileUtility.writeText(target, content.isBlank() ? "Sin mensajes de consola." : content);
        } catch (RuntimeException unsupported) {
            FileUtility.writeText(target, "El navegador no expuso logs de consola: " + unsupported.getMessage());
        }
    }
}
