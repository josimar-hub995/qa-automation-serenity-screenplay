package pe.com.challenge.automation.hooks;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import org.opentest4j.TestAbortedException;
import pe.com.challenge.automation.exceptions.TestDataException;
import pe.com.challenge.automation.libraries.DataWorkbookLibrary;
import pe.com.challenge.automation.managers.DataSourceManager;
import pe.com.challenge.automation.managers.EvidenceManager;
import pe.com.challenge.automation.managers.ExecutionManager;
import pe.com.challenge.automation.managers.ScenarioContext;
import pe.com.challenge.automation.managers.ScenarioManager;
import pe.com.challenge.automation.models.DataSourceDescriptor;
import pe.com.challenge.automation.reports.ReportManager;
import pe.com.challenge.automation.utilities.StringUtility;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hooks {

    private static final Pattern DATA_ID_PATTERN =
            Pattern.compile("(?i)(?:datos|fila)\\s+(\\d+)\\s*$");

    @BeforeAll
    public static void beforeAll() {
        ExecutionManager.startExecution();
    }

    @Before(order = 0)
    public void beforeScenario(Scenario scenario) {

        ScenarioManager.startScenario(scenario);

        try {
            DataSourceManager.configure(scenario.getSourceTagNames());

            ScenarioManager.setDataSource(
                    DataSourceManager.current()
            );

            validateExecution(scenario);

            OnStage.setTheStage(new OnlineCast());
            OnStage.theActorCalled("Usuario QA");

        } catch (TestAbortedException aborted) {

            ScenarioManager.log(aborted.getMessage());

            throw aborted;

        } catch (RuntimeException | AssertionError setupError) {

            ScenarioManager.recordFrameworkFailure(
                    "Preparación del escenario",
                    setupError
            );

            throw setupError;
        }
    }

    @After(order = 100)
    public void afterScenario(Scenario scenario) {

        try {

            if (!"SKIPPED".equalsIgnoreCase(
                    scenario.getStatus().name())) {

                Actor actor = OnStage.theActorInTheSpotlight();

                if (!ScenarioManager.isApiScenario()) {

                    if (scenario.isFailed()) {

                        byte[] finalEvidence = EvidenceManager.capture(
                                actor,
                                "estado_final_failed"
                        );

                        scenario.attach(
                                finalEvidence,
                                "image/png",
                                "Estado final del fallo"
                        );
                    }

                    EvidenceManager.saveBrowserConsole(actor);

                } else {

                    ScenarioManager.log(
                            "Escenario API: evidencia disponible en api_exchange.json, api_response.json y Serenity."
                    );
                }
            }

        } catch (RuntimeException evidenceError) {

            ScenarioManager.log(
                    "No se pudo completar la evidencia final: "
                            + evidenceError.getMessage()
            );

        } finally {

            try {

                ScenarioManager.finishScenario(
                        scenario.getStatus().name()
                );

            } finally {

                DataSourceManager.clear();
                ScenarioContext.clear();

                try {
                    OnStage.drawTheCurtain();
                } catch (RuntimeException ignored) {
                }
            }
        }
    }

    @AfterAll
    public static void afterAll() {
        ReportManager.generateExecutionReports();
    }

    private void validateExecution(Scenario scenario) {

        String id = extractDataId(scenario.getName());

        Map<String, String> row =
                DataWorkbookLibrary.dataset(id);

        String executeValue = row.get("EJECUTAR");

        if (executeValue == null || executeValue.isBlank()) {

            throw new TestDataException(
                    "La columna obligatoria 'EJECUTAR' no tiene valor"
            );
        }

        if (!StringUtility.isTrue(executeValue)) {

            DataSourceDescriptor source =
                    DataSourceManager.current();

            throw new TestAbortedException(
                    "El conjunto de datos "
                            + source.sheetName()
                            + "/"
                            + id
                            + " está marcado con EJECUTAR=NO"
            );
        }
    }

    private String extractDataId(String scenarioName) {

        Matcher matcher =
                DATA_ID_PATTERN.matcher(scenarioName);

        if (!matcher.find()) {

            throw new TestDataException(
                    "No se pudo identificar el dato de prueba en el escenario: "
                            + scenarioName
            );
        }

        return matcher.group(1);
    }
}