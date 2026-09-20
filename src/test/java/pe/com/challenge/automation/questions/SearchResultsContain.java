package pe.com.challenge.automation.questions;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import pe.com.challenge.automation.locators.SearchLocators;
import pe.com.challenge.automation.utilities.StringUtility;

public final class SearchResultsContain implements Question<Boolean> {
    private final String expected;

    private SearchResultsContain(String expected) {
        this.expected = expected;
    }

    public static SearchResultsContain expected(String expected) {
        return new SearchResultsContain(expected);
    }

    @Override
    public Boolean answeredBy(Actor actor) {
        String normalizedExpected = StringUtility.normalizeForComparison(expected);
        return SearchLocators.SEARCH_RESULTS.resolveAllFor(actor).stream()
                .map(element -> StringUtility.normalizeForComparison(element.getText()))
                .anyMatch(text -> text.contains(normalizedExpected));
    }

    @Override
    public String getSubject() {
        return "los resultados contienen '" + expected + "'";
    }
}
