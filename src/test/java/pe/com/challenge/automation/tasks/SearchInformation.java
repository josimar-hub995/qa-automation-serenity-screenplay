package pe.com.challenge.automation.tasks;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.actions.Click;
import pe.com.challenge.automation.interactions.EnterSearchQuery;
import pe.com.challenge.automation.interactions.WaitFor;
import pe.com.challenge.automation.locators.HeaderLocators;
import pe.com.challenge.automation.locators.SearchLocators;
import pe.com.challenge.automation.models.SearchTestData;

public class SearchInformation implements Task {
    private final SearchTestData data;

    public SearchInformation(SearchTestData data) {
        this.data = data;
    }

    public static SearchInformation with(SearchTestData data) {
        return Tasks.instrumented(SearchInformation.class, data);
    }

    @Override
    @Step("{0} busca '#data.searchValue' en Selenium")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                WaitFor.clickable(
                        HeaderLocators.SEARCH_BUTTON,
                        "habilitar el botón de búsqueda de Selenium"),
                Click.on(HeaderLocators.SEARCH_BUTTON),
                WaitFor.visible(
                        SearchLocators.SEARCH_MODAL,
                        "abrir el modal DocSearch"),
                WaitFor.visible(
                        SearchLocators.SEARCH_INPUT,
                        "mostrar el campo docsearch-input"),
                EnterSearchQuery.value(data.searchValue()),
                WaitFor.visible(
                        SearchLocators.SEARCH_RESULTS,
                        "mostrar resultados para la consulta '" + data.searchValue() + "'")
        );
    }
}
