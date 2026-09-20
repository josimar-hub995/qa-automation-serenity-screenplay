package pe.com.challenge.automation.models;

public record HomeTestData(
        String id,
        String testCaseId,
        String scenarioName,
        String expectedResult,
        boolean execute) implements ScenarioTestData {
}
