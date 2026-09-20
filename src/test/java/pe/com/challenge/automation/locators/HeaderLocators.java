package pe.com.challenge.automation.locators;

import net.serenitybdd.screenplay.targets.Target;

public final class HeaderLocators {
    public static final Target SEARCH_BUTTON = Target.the("botón de búsqueda")
            .locatedBy("button.DocSearch-Button, button[aria-label*='Search'], button[aria-label*='search']");

    private HeaderLocators() {
    }

    public static Target navigationOption(String menuText) {
        return Target.the("opción de navegación " + menuText)
                .locatedBy("//nav//a[contains(@class,'nav-link') and normalize-space()='{0}']")
                .of(menuText);
    }
}
