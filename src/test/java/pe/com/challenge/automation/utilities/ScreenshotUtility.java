package pe.com.challenge.automation.utilities;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import pe.com.challenge.automation.exceptions.FrameworkException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ScreenshotUtility {
    private ScreenshotUtility() {
    }

    public static byte[] capture(WebDriver driver, Path target) {
        if (!(driver instanceof TakesScreenshot screenshotDriver)) {
            throw new FrameworkException("El navegador no permite tomar capturas de pantalla");
        }
        byte[] evidence = screenshotDriver.getScreenshotAs(OutputType.BYTES);
        try {
            FileUtility.createDirectories(target.getParent());
            Files.write(target, evidence);
            return evidence;
        } catch (IOException e) {
            throw new FrameworkException("No se pudo guardar la captura: " + target, e);
        }
    }
}
