package pe.com.challenge.automation.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import pe.com.challenge.automation.locators.DocumentationLocators;
import pe.com.challenge.automation.managers.ConfigurationManager;
import pe.com.challenge.automation.utilities.StringUtility;

public final class DocumentationMatches implements Question<Boolean> {
    private DocumentationMatches() {
    }

    public static DocumentationMatches expected() {
        return new DocumentationMatches();
    }

    @Override
    public Boolean answeredBy(Actor actor) {
        String url = BrowseTheWeb.as(actor).getDriver().getCurrentUrl();
        String heading = DocumentationLocators.MAIN_HEADING.resolveFor(actor).getText();
        return StringUtility.normalizeForComparison(url)
                .contains(StringUtility.normalizeForComparison(
                        ConfigurationManager.get("documentation.expected.url.fragment")))
                && StringUtility.normalizeForComparison(heading)
                .contains(StringUtility.normalizeForComparison(
                        ConfigurationManager.get("documentation.expected.heading")));
    }

    @Override
    public String getSubject() {
        return "la documentación coincide con el resultado esperado";
    }
}
