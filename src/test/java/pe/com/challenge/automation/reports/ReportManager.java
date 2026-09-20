package pe.com.challenge.automation.reports;

import pe.com.challenge.automation.constants.FrameworkConstants;
import pe.com.challenge.automation.managers.ExecutionManager;
import pe.com.challenge.automation.models.ScenarioExecutionResult;
import pe.com.challenge.automation.models.StepExecutionResult;
import pe.com.challenge.automation.utilities.DateUtility;
import pe.com.challenge.automation.utilities.JsonUtility;
import pe.com.challenge.automation.utilities.ResourceUtility;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ReportManager {

    private ReportManager() {
    }

    public static void generateExecutionReports() {

        List<ScenarioExecutionResult> results =
                ExecutionManager.results();

        Path execution =
                ExecutionManager.executionPath();

        Path summary =
                execution.resolve("resumen");

        Path assets =
                summary.resolve("assets");

        Path logo = ResourceUtility.copyTo(
                FrameworkConstants.LOGO_RESOURCE,
                assets.resolve("qa_automation_logo.png")
        );

        int passed = (int) results.stream()
                .filter(result ->
                        "PASSED".equalsIgnoreCase(result.status()))
                .count();

        int failed = (int) results.stream()
                .filter(result ->
                        "FAILED".equalsIgnoreCase(result.status()))
                .count();

        List<Path> errorReports =
                new ArrayList<>();

        for (ScenarioExecutionResult result : results) {

            Path word = execution
                    .resolve("word")
                    .resolve(
                            ReportNaming.wordFileName(result)
                    );

            WordReportGenerator.generate(
                    result,
                    logo,
                    word
            );

            if ("FAILED".equalsIgnoreCase(
                    result.status())) {

                errorReports.add(
                        ScenarioErrorReportGenerator.generate(
                                result,
                                result.scenarioPath()
                                        .resolve(
                                                "error-report.html"
                                        )
                        )
                );
            }
        }

        Path htmlDashboard =
                summary.resolve(
                        "dashboard.html"
                );

        HtmlDashboardGenerator.generate(
                results,
                htmlDashboard
        );

        Map<String, Object> executionSummary =
                new LinkedHashMap<>();

        executionSummary.put(
                "report",
                execution.getFileName().toString()
        );

        executionSummary.put(
                "executionNumber",
                ExecutionManager.executionNumber()
        );

        executionSummary.put(
                "startTime",
                DateUtility.display(
                        ExecutionManager.startedAt()
                )
        );

        executionSummary.put(
                "endTime",
                DateUtility.display(
                        LocalDateTime.now()
                )
        );

        executionSummary.put(
                "total",
                results.size()
        );

        executionSummary.put(
                "passed",
                passed
        );

        executionSummary.put(
                "failed",
                failed
        );

        executionSummary.put(
                "successRate",
                results.isEmpty()
                        ? 0
                        : passed * 100.0
                        / results.size()
        );

        executionSummary.put(
                "durationSeconds",
                results.stream()
                        .map(
                                ScenarioExecutionResult::duration
                        )
                        .mapToDouble(
                                Duration::toMillis
                        )
                        .sum()
                        / 1000.0
        );

        executionSummary.put(
                "scenarios",
                results.stream()
                        .map(result -> {

                            Map<String, Object> scenario =
                                    new LinkedHashMap<>();

                            scenario.put(
                                    "tap",
                                    result.testCaseId()
                            );

                            scenario.put(
                                    "tag",
                                    "@"
                                            + result.executionTag()
                            );

                            scenario.put(
                                    "scenario",
                                    result.scenarioName()
                            );

                            scenario.put(
                                    "expectedResult",
                                    result.expectedResult()
                            );

                            scenario.put(
                                    "datos",
                                    result.datasetId()
                            );

                            scenario.put(
                                    "workbook",
                                    result.workbookName()
                            );

                            scenario.put(
                                    "sheet",
                                    result.sheetName()
                            );

                            scenario.put(
                                    "status",
                                    result.status()
                            );

                            scenario.put(
                                    "browser",
                                    result.browser()
                            );

                            scenario.put(
                                    "targetUrl",
                                    result.targetUrl()
                            );

                            scenario.put(
                                    "failedStep",
                                    result.failedStep()
                            );

                            scenario.put(
                                    "errorMessage",
                                    result.errorMessage()
                            );

                            scenario.put(
                                    "apiExchange",
                                    result.apiExchange()
                            );

                            scenario.put(
                                    "wordReport",
                                    "../word/"
                                            + ReportNaming
                                            .wordFileName(result)
                            );

                            scenario.put(
                                    "errorReport",
                                    "FAILED".equalsIgnoreCase(
                                            result.status()
                                    )
                                            ? "../escenarios/"
                                            + result.scenarioPath()
                                            .getFileName()
                                            + "/error-report.html"
                                            : ""
                            );

                            scenario.put(
                                    "steps",
                                    result.steps()
                                            .stream()
                                            .map(
                                                    ReportManager::stepSummary
                                            )
                                            .toList()
                            );

                            return scenario;
                        })
                        .toList()
        );

        JsonUtility.write(
                summary.resolve(
                        "execution_summary.json"
                ),
                executionSummary
        );

        ExecutionExcelReportGenerator.generate(
                results,
                summary.resolve(
                        "resultado_ejecucion.xlsx"
                )
        );

        Path serenityReport =
                Path.of(
                                "target",
                                "site",
                                "serenity",
                                "index.html"
                        )
                        .toAbsolutePath()
                        .normalize();

        Path archivedSerenityReport =
                execution
                        .resolve("serenity")
                        .resolve("index.html")
                        .toAbsolutePath()
                        .normalize();

        ExecutionConsole.printReportLinks(
                htmlDashboard,
                serenityReport,
                archivedSerenityReport,
                errorReports,
                results.size(),
                passed,
                failed
        );
    }

    private static Map<String, Object> stepSummary(
            StepExecutionResult step) {

        Map<String, Object> value =
                new LinkedHashMap<>();

        value.put(
                "sequence",
                step.sequence()
        );

        value.put(
                "gherkin",
                step.gherkinText()
        );

        value.put(
                "status",
                step.status()
        );

        value.put(
                "startTime",
                DateUtility.stepDisplay(
                        step.startTime()
                )
        );

        value.put(
                "durationSeconds",
                step.duration()
                        .toMillis()
                        / 1000.0
        );

        value.put(
                "errorMessage",
                step.errorMessage()
        );

        value.put(
                "screenshots",
                step.screenshots()
                        .stream()
                        .map(
                                path ->
                                        path.getFileName()
                                                .toString()
                        )
                        .toList()
        );

        return value;
    }
}