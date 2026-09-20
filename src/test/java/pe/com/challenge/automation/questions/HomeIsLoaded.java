package pe.com.challenge.automation.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import pe.com.challenge.automation.locators.HomeLocators;

public final class HomeIsLoaded implements Question<Boolean> {
    public static HomeIsLoaded correctly() {
        return new HomeIsLoaded();
    }

    @Override
    public Boolean answeredBy(Actor actor) {
        return HomeLocators.MAIN_HEADING.resolveFor(actor).isVisible()
                && HomeLocators.SELENIUM_LOGO.resolveFor(actor).isVisible();
    }

    @Override
    public String getSubject() {
        return "la página principal está cargada";
    }
}
