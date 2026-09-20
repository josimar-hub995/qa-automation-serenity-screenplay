package pe.com.challenge.automation.models;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record ScenarioExecutionResult(
        String testCaseId,
        String executionTag,
        String scenarioName,
        String expectedResult,
        String datasetId,
        String workbookName,
        String sheetName,
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Duration duration,
        Path scenarioPath,
        String browser,
        String targetUrl,
        List<StepExecutionResult> steps,
        List<Path> screenshots,
        String failedStep,
        String errorMessage,
        String stackTrace,
        ApiExchange apiExchange) {

    public ScenarioExecutionResult {
        steps = steps == null ? List.of() : List.copyOf(steps);
        screenshots = screenshots == null ? List.of() : List.copyOf(screenshots);
        browser = browser == null ? "N/A" : browser;
        targetUrl = targetUrl == null ? "N/A" : targetUrl;
        failedStep = failedStep == null ? "" : failedStep;
        errorMessage = errorMessage == null ? "" : errorMessage;
        stackTrace = stackTrace == null ? "" : stackTrace;
        apiExchange = apiExchange == null ? ApiExchange.empty() : apiExchange;
    }
}
