package pe.com.challenge.automation.locators;

import net.serenitybdd.screenplay.targets.Target;

public final class DocumentationLocators {
    public static final Target MAIN_HEADING = Target.the("título principal de la documentación")
            .locatedBy("//h1[contains(normalize-space(), 'Selenium Browser Automation Project')]");

    public static final Target DOCUMENTATION_CONTENT = Target.the("contenido de la documentación")
            .locatedBy("article, .td-content");

    private DocumentationLocators() {
    }
}
