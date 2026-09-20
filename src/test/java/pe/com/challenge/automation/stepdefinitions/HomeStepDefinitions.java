package pe.com.challenge.automation.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import net.serenitybdd.screenplay.ensure.Ensure;
import pe.com.challenge.automation.managers.ConfigurationManager;
import pe.com.challenge.automation.managers.DataTestManager;
import pe.com.challenge.automation.managers.ScenarioManager;
import pe.com.challenge.automation.models.HomeTestData;
import pe.com.challenge.automation.questions.HomeIsLoaded;
import pe.com.challenge.automation.questions.PageTitleContains;
import pe.com.challenge.automation.tasks.OpenSeleniumHome;
import pe.com.challenge.automation.utilities.GherkinStepExecutor;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class HomeStepDefinitions {
    @Given("que el usuario accede al sitio web de Selenium")
    public void enterSelenium() {
        GherkinStepExecutor.execute(
                "Given",
                "que el usuario accede al sitio web de Selenium",
                () -> theActorInTheSpotlight().wasAbleTo(OpenSeleniumHome.now()));
    }

    @Then("la página de inicio debe mostrarse correctamente usando los datos {string}")
    public void validateHome(String dataId) {
        GherkinStepExecutor.execute(
                "Then",
                "la página de inicio debe mostrarse correctamente usando los datos \"%s\"".formatted(dataId),
                () -> {
                    HomeTestData data = DataTestManager.getHomeData(dataId);
                    ScenarioManager.setDataset(dataId, data);
                    theActorInTheSpotlight().attemptsTo(
                            Ensure.that(HomeIsLoaded.correctly()).isTrue(),
                            Ensure.that(PageTitleContains.expected(
                                    ConfigurationManager.get("home.expected.title"))).isTrue());
                });
    }
}
