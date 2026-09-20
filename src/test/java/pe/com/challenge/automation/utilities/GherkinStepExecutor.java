package pe.com.challenge.automation.utilities;

import pe.com.challenge.automation.managers.EvidenceManager;
import pe.com.challenge.automation.managers.ScenarioManager;

/**
 * Registra el ciclo de vida real de cada paso Gherkin y mantiene asociadas
 * sus evidencias, duración y posible error técnico.
 */
public final class GherkinStepExecutor {
    private GherkinStepExecutor() {
    }

    public static void execute(String keyword, String text, Runnable action) {
        ScenarioManager.startStep(keyword, text);
        try {
            action.run();
            EvidenceManager.captureCurrentStepIfWeb("paso_%02d_%s"
                    .formatted(ScenarioManager.currentStepNumber(), keyword.toLowerCase()));
        } catch (RuntimeException | AssertionError error) {
            EvidenceManager.captureCurrentStepIfWeb("paso_%02d_failed"
                    .formatted(ScenarioManager.currentStepNumber()));
            ScenarioManager.finishStep("FAILED", error);
            throw error;
        }
        ScenarioManager.finishStep("PASSED", null);
    }
}
