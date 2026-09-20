package pe.com.challenge.automation.models;

public record SearchTestData(
        String id,
        String testCaseId,
        String scenarioName,
        String expectedResult,
        String searchValue,
        boolean execute) implements ScenarioTestData {
}
