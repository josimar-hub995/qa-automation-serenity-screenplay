package pe.com.challenge.automation.models;

public interface ScenarioTestData {
    String id();

    String testCaseId();

    String scenarioName();

    String expectedResult();

    boolean execute();
}
