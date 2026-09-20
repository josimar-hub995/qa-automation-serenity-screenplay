package pe.com.challenge.automation.tasks;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.actions.Click;
import pe.com.challenge.automation.interactions.WaitFor;
import pe.com.challenge.automation.locators.DocumentationLocators;
import pe.com.challenge.automation.locators.HeaderLocators;
import pe.com.challenge.automation.managers.ConfigurationManager;

public class NavigateToDocumentation implements Task {
    public NavigateToDocumentation() {
    }

    public static NavigateToDocumentation now() {
        return Tasks.instrumented(NavigateToDocumentation.class);
    }

    @Override
    @Step("{0} abre el apartado de documentación")
    public <T extends Actor> void performAs(T actor) {
        String menuText = ConfigurationManager.get("documentation.menu.text");
        actor.attemptsTo(
                WaitFor.clickable(
                        HeaderLocators.navigationOption(menuText),
                        "habilitar el menú de navegación '" + menuText + "'"),
                Click.on(HeaderLocators.navigationOption(menuText)),
                WaitFor.visible(
                        DocumentationLocators.MAIN_HEADING,
                        "cargar el encabezado principal de Documentation")
        );
    }
}
