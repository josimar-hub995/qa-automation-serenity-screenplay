package pe.com.challenge.automation.stepdefinitions;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.screenplay.ensure.Ensure;
import pe.com.challenge.automation.managers.DataTestManager;
import pe.com.challenge.automation.managers.ScenarioContext;
import pe.com.challenge.automation.managers.ScenarioManager;
import pe.com.challenge.automation.models.DocumentationTestData;
import pe.com.challenge.automation.questions.DocumentationMatches;
import pe.com.challenge.automation.tasks.NavigateToDocumentation;
import pe.com.challenge.automation.utilities.GherkinStepExecutor;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class DocumentationStepDefinitions {
    private static final String DOCUMENTATION_DATA = "documentation.data";

    @When("el usuario abre la documentación usando los datos {string}")
    public void openDocumentation(String dataId) {
        GherkinStepExecutor.execute(
                "When",
                "el usuario abre la documentación usando los datos \"%s\"".formatted(dataId),
                () -> {
                    DocumentationTestData data = DataTestManager.getDocumentationData(dataId);
                    ScenarioContext.put(DOCUMENTATION_DATA, data);
                    ScenarioManager.setDataset(dataId, data);
                    theActorInTheSpotlight().attemptsTo(NavigateToDocumentation.now());
                });
    }

    @Then("la página de documentación esperada debe mostrarse usando los datos {string}")
    public void validateDocumentation(String dataId) {
        GherkinStepExecutor.execute(
                "Then",
                "la página de documentación esperada debe mostrarse usando los datos \"%s\"".formatted(dataId),
                () -> {
                    DocumentationTestData data = ScenarioContext.get(
                            DOCUMENTATION_DATA, DocumentationTestData.class);
                    if (!data.id().equals(dataId)) {
                        throw new IllegalStateException(
                                "El dataset validado no coincide con el dataset utilizado: " + dataId);
                    }
                    theActorInTheSpotlight().attemptsTo(
                            Ensure.that(DocumentationMatches.expected()).isTrue());
                });
    }
}
