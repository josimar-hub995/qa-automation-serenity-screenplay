package pe.com.challenge.automation.managers;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import net.serenitybdd.screenplay.actors.OnStage;
import org.openqa.selenium.WebDriver;
import pe.com.challenge.automation.utilities.BrowserConsoleUtility;
import pe.com.challenge.automation.utilities.ScreenshotUtility;

import java.nio.file.Path;

public final class EvidenceManager {
    private EvidenceManager() {
    }

    public static byte[] capture(Actor actor, String label) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        try {
            ScenarioManager.recordTargetUrl(driver.getCurrentUrl());
        } catch (RuntimeException urlError) {
            ScenarioManager.log("No se pudo consultar la URL actual: " + urlError.getMessage());
        }
        Path screenshot = ScenarioManager.nextScreenshotPath(label);
        byte[] bytes = ScreenshotUtility.capture(driver, screenshot);
        ScenarioManager.recordScreenshot(screenshot);
        ScenarioManager.log("Evidencia guardada: " + screenshot.getFileName());
        return bytes;
    }

    public static void captureCurrentStepIfWeb(String label) {
        if (ScenarioManager.isApiScenario()) {
            return;
        }
        try {
            capture(OnStage.theActorInTheSpotlight(), label);
        } catch (RuntimeException evidenceError) {
            ScenarioManager.log("No se pudo capturar evidencia del paso: " + evidenceError.getMessage());
        }
    }

    public static void saveBrowserConsole(Actor actor) {
        WebDriver driver = BrowseTheWeb.as(actor).getDriver();
        BrowserConsoleUtility.save(driver, ScenarioManager.currentPath().resolve("browser-console/console.log"));
    }
}
