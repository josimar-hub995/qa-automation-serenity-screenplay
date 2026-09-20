package pe.com.challenge.automation.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;
import pe.com.challenge.automation.utilities.StringUtility;

public final class PageTitleContains implements Question<Boolean> {
    private final String expected;

    private PageTitleContains(String expected) {
        this.expected = expected;
    }

    public static PageTitleContains expected(String expected) {
        return new PageTitleContains(expected);
    }

    @Override
    public Boolean answeredBy(Actor actor) {
        String title = BrowseTheWeb.as(actor).getDriver().getTitle();
        return StringUtility.normalizeForComparison(title)
                .contains(StringUtility.normalizeForComparison(expected));
    }

    @Override
    public String getSubject() {
        return "el título de la página contiene '" + expected + "'";
    }
}
