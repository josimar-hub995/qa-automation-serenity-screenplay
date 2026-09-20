package pe.com.challenge.automation.locators;

import net.serenitybdd.screenplay.targets.Target;

public final class HomeLocators {
    public static final Target MAIN_HEADING = Target.the("título principal de Selenium")
            .locatedBy("//h1[contains(normalize-space(), 'Selenium automates browsers')]");

    public static final Target SELENIUM_LOGO = Target.the("logotipo de Selenium")
            .locatedBy("nav a.navbar-brand, a.navbar-brand");

    private HomeLocators() {
    }
}
