package pe.com.challenge.automation.models;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record StepExecutionResult(
        int sequence,
        String keyword,
        String text,
        String status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Duration duration,
        List<Path> screenshots,
        String errorType,
        String errorMessage,
        String stackTrace) {

    public StepExecutionResult {
        screenshots = screenshots == null ? List.of() : List.copyOf(screenshots);
        errorType = errorType == null ? "" : errorType;
        errorMessage = errorMessage == null ? "" : errorMessage;
        stackTrace = stackTrace == null ? "" : stackTrace;
    }

    public String gherkinText() {
        return keyword + " " + text;
    }
}
