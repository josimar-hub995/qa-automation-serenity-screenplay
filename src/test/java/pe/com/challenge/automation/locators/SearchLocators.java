package pe.com.challenge.automation.locators;

import net.serenitybdd.screenplay.targets.Target;

public final class SearchLocators {
    public static final Target SEARCH_INPUT = Target.the("campo de búsqueda")
            .locatedBy("#docsearch-input");

    public static final Target SEARCH_RESULTS = Target.the("resultados de búsqueda")
            .locatedBy("#docsearch-list .DocSearch-Hit a, .DocSearch-Dropdown .DocSearch-Hit a");

    public static final Target SEARCH_MODAL = Target.the("ventana de búsqueda")
            .locatedBy(".DocSearch-Modal");

    private SearchLocators() {
    }
}
