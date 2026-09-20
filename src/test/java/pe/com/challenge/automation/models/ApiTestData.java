package pe.com.challenge.automation.models;

public record ApiTestData(
        String id,
        String testCaseId,
        String scenarioName,
        String expectedResult,
        int iteration,
        String layer,
        String version,
        String apiName,
        String credentials,
        String endpoint,
        String headers,
        String headerErrorConfig,
        String params,
        String requestType,
        String body,
        int expectedStatus,
        String expectedResponse,
        boolean execute) implements ScenarioTestData {
}
