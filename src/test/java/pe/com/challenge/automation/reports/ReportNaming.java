package pe.com.challenge.automation.reports;

import pe.com.challenge.automation.managers.ExecutionManager;
import pe.com.challenge.automation.models.ScenarioExecutionResult;
import pe.com.challenge.automation.utilities.StringUtility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ReportNaming {
    private static final Pattern TAP_NUMBER = Pattern.compile("(?i)^TAP\\s*-\\s*(\\d+)$");

    private ReportNaming() {
    }

    public static String wordFileName(ScenarioExecutionResult result) {
        return "%s_REPORT_%03d.docx".formatted(
                tapFileToken(result.testCaseId()),
                ExecutionManager.executionNumber());
    }

    private static String tapFileToken(String tap) {
        Matcher matcher = TAP_NUMBER.matcher(tap == null ? "" : tap.trim());
        return matcher.matches()
                ? "TAP_" + matcher.group(1)
                : StringUtility.safeFileName(tap);
    }
}
