package pe.com.challenge.automation.tasks;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.actions.Open;
import pe.com.challenge.automation.interactions.WaitFor;
import pe.com.challenge.automation.managers.ConfigurationManager;
import pe.com.challenge.automation.managers.ScenarioManager;
import pe.com.challenge.automation.locators.HomeLocators;

public class OpenSeleniumHome implements Task {
    public OpenSeleniumHome() {
    }

    public static OpenSeleniumHome now() {
        return Tasks.instrumented(OpenSeleniumHome.class);
    }

    @Override
    @Step("{0} abre la página principal de Selenium")
    public <T extends Actor> void performAs(T actor) {
        String url = ConfigurationManager.get("base.url");
        ScenarioManager.recordTargetUrl(url);
        actor.attemptsTo(
                Open.url(url),
                WaitFor.visible(
                        HomeLocators.MAIN_HEADING,
                        "cargar el encabezado principal de Selenium en " + url)
        );
    }
}
