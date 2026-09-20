package pe.com.challenge.automation.stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.ensure.Ensure;
import pe.com.challenge.automation.managers.DataTestManager;
import pe.com.challenge.automation.managers.ScenarioContext;
import pe.com.challenge.automation.managers.ScenarioManager;
import pe.com.challenge.automation.models.SearchTestData;
import pe.com.challenge.automation.questions.SearchResultsContain;
import pe.com.challenge.automation.tasks.SearchInformation;
import pe.com.challenge.automation.utilities.GherkinStepExecutor;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class SearchStepDefinitions {
    private static final String SEARCH_DATA = "search.data";

    @When("el usuario realiza una búsqueda usando los datos {string}")
    public void search(String dataId) {
        GherkinStepExecutor.execute(
                "When",
                "el usuario realiza una búsqueda usando los datos \"%s\"".formatted(dataId),
                () -> {
                    SearchTestData data = DataTestManager.getSearchData(dataId);
                    ScenarioContext.put(SEARCH_DATA, data);
                    ScenarioManager.setDataset(dataId, data);
                    theActorInTheSpotlight().attemptsTo(SearchInformation.with(data));
                });
    }

    @Then("deben mostrarse resultados relacionados usando los datos {string}")
    public void validateSearchResults(String dataId) {
        GherkinStepExecutor.execute(
                "Then",
                "deben mostrarse resultados relacionados usando los datos \"%s\"".formatted(dataId),
                () -> {
                    SearchTestData data = ScenarioContext.get(SEARCH_DATA, SearchTestData.class);
                    if (!data.id().equals(dataId)) {
                        throw new IllegalStateException(
                                "El dataset validado no coincide con el dataset utilizado: " + dataId);
                    }
                    theActorInTheSpotlight().attemptsTo(
                            Ensure.that(SearchResultsContain.expected(data.searchValue())).isTrue());
                });
    }
}
