package pe.com.challenge.automation.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.ensure.Ensure;
import pe.com.challenge.automation.managers.DataTestManager;
import pe.com.challenge.automation.managers.ScenarioContext;
import pe.com.challenge.automation.managers.ScenarioManager;
import pe.com.challenge.automation.models.ApiTestData;
import pe.com.challenge.automation.questions.ApiResponseMatches;
import pe.com.challenge.automation.tasks.ExecuteApiRequest;
import pe.com.challenge.automation.utilities.GherkinStepExecutor;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class ApiStepDefinitions {
    private static final String API_DATA = "api.test.data";

    @Given("se cargan los datos del servicio API identificados como {string}")
    public void loadApiData(String dataId) {
        GherkinStepExecutor.execute(
                "Given",
                "se cargan los datos del servicio API identificados como \"%s\"".formatted(dataId),
                () -> {
                    ApiTestData data = DataTestManager.getApiData(dataId);
                    ScenarioContext.put(API_DATA, data);
                    ScenarioManager.setDataset(dataId, data);
                });
    }

    @When("se envía la petición API configurada con los datos {string}")
    public void sendConfiguredRequest(String dataId) {
        GherkinStepExecutor.execute(
                "When",
                "se envía la petición API configurada con los datos \"%s\"".formatted(dataId),
                () -> theActorInTheSpotlight().attemptsTo(
                        ExecuteApiRequest.configuredWith(contextData(dataId))));
    }

    @Then("se valida el status code y el response esperados con los datos {string}")
    public void validateConfiguredResponse(String dataId) {
        GherkinStepExecutor.execute(
                "Then",
                "se valida el status code y el response esperados con los datos \"%s\"".formatted(dataId),
                () -> theActorInTheSpotlight().attemptsTo(
                        Ensure.that(ApiResponseMatches.expected(contextData(dataId))).isTrue()));
    }

    private ApiTestData contextData(String dataId) {
        ApiTestData data = ScenarioContext.get(API_DATA, ApiTestData.class);
        if (!data.id().equals(dataId)) {
            throw new IllegalStateException(
                    "Los datos validados no coinciden con los utilizados en la petición: " + dataId);
        }
        return data;
    }
}
