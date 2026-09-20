package pe.com.challenge.automation.hooks;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import pe.com.challenge.automation.managers.DataSourceManager;
import pe.com.challenge.automation.managers.EvidenceManager;
import pe.com.challenge.automation.managers.ExecutionManager;
import pe.com.challenge.automation.managers.ScenarioContext;
import pe.com.challenge.automation.managers.ScenarioManager;
import pe.com.challenge.automation.reports.ReportManager;

public class Hooks {
    @BeforeAll
    public static void beforeAll() {
        ExecutionManager.startExecution();
    }

    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {
        ScenarioManager.startScenario(scenario);
        try {
            DataSourceManager.configure(scenario.getSourceTagNames());
            ScenarioManager.setDataSource(DataSourceManager.current());
            OnStage.setTheStage(new OnlineCast());
            OnStage.theActorCalled("Usuario QA");
        } catch (RuntimeException | AssertionError setupError) {
            ScenarioManager.recordFrameworkFailure("Preparación del escenario", setupError);
            throw setupError;
        }
    }

    @After(order = 100)
    public void afterScenario(Scenario scenario) {
        try {
            Actor actor = OnStage.theActorInTheSpotlight();
            if (!ScenarioManager.isApiScenario()) {
                if (scenario.isFailed()) {
                    byte[] finalEvidence = EvidenceManager.capture(actor, "estado_final_failed");
                    scenario.attach(finalEvidence, "image/png", "Estado final del fallo");
                }
                EvidenceManager.saveBrowserConsole(actor);
            } else {
                ScenarioManager.log("Escenario API: evidencia disponible en api_exchange.json, api_response.json y Serenity.");
            }
        } catch (RuntimeException evidenceError) {
            ScenarioManager.log("No se pudo completar la evidencia final: " + evidenceError.getMessage());
        } finally {
            try {
                ScenarioManager.finishScenario(scenario.getStatus().name());
            } finally {
                DataSourceManager.clear();
                ScenarioContext.clear();
                try {
                    OnStage.drawTheCurtain();
                } catch (RuntimeException ignored) {
                    // The stage may not exist when a configuration hook fails early.
                }
            }
        }
    }

    @AfterAll
    public static void afterAll() {
        ReportManager.generateExecutionReports();
    }
}
