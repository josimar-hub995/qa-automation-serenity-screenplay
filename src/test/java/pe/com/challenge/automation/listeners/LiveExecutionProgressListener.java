package pe.com.challenge.automation.listeners;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import pe.com.challenge.automation.utilities.ExecutionLogging;
import pe.com.challenge.automation.utilities.ExecutionSettings;
import pe.com.challenge.automation.utilities.CucumberTagExpression;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Muestra únicamente el avance funcional de la ejecución. Los logs técnicos se
 * controlan por separado con la propiedad {@code execution.logs}.
 */
public final class LiveExecutionProgressListener implements TestExecutionListener {
    private static final int BAR_WIDTH = 24;
    private static final int CONSOLE_WIDTH = 112;
    private static final String MAIN_LINE = "=".repeat(CONSOLE_WIDTH);
    private static final String SEPARATOR = "-".repeat(CONSOLE_WIDTH);

    private final AtomicInteger total = new AtomicInteger();
    private final AtomicInteger completed = new AtomicInteger();
    private final AtomicInteger passed = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicBoolean summaryPrinted = new AtomicBoolean();
    private boolean detailedLogs;
    private String tagExpression;

    public LiveExecutionProgressListener() {
        ExecutionLogging.configure();
    }

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        ExecutionLogging.configure();
        detailedLogs = ExecutionSettings.detailedLogsEnabled();
        tagExpression = System.getProperty("cucumber.filter.tags", "").trim();
        total.set(Math.toIntExact(testPlan.countTestIdentifiers(
                identifier -> identifier.isTest() && isSelected(identifier))));
        completed.set(0);
        passed.set(0);
        failed.set(0);
        summaryPrinted.set(false);

        if (total.get() > 0) {
            synchronized (System.out) {
                System.out.printf("%n%s%n", MAIN_LINE);
                printCentered("QA AUTOMATION | EJECUCION DE PRUEBAS");
                System.out.println(MAIN_LINE);
                System.out.printf(" Casos seleccionados : %d%n", total.get());
                System.out.printf(" Modo de consola      : %s%n", detailedLogs ? "DETALLADO" : "RESUMIDO");
                System.out.println(SEPARATOR);
            }
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (!testIdentifier.isTest() || !isSelected(testIdentifier)) {
            return;
        }

        switch (testExecutionResult.getStatus()) {
            case SUCCESSFUL -> passed.incrementAndGet();
            case FAILED, ABORTED -> failed.incrementAndGet();
        }

        int executed = completed.incrementAndGet();
        int planned = Math.max(total.get(), executed);
        int remaining = Math.max(planned - executed, 0);
        int percentage = planned == 0 ? 100 : (int) Math.round(executed * 100.0 / planned);

        synchronized (System.out) {
            System.out.printf(
                    " [PROGRESO] [%s] %3d%% | Completados %d/%d | PASSED %d | FAILED %d | Pendientes %d%n",
                    progressBar(percentage),
                    percentage,
                    executed,
                    planned,
                    passed.get(),
                    failed.get(),
                    remaining);

            if (detailedLogs) {
                System.out.printf(
                        " [DETALLE]  %-9s | %s%n",
                        displayStatus(testExecutionResult.getStatus()),
                        singleLine(testIdentifier.getDisplayName()));
                testExecutionResult.getThrowable().ifPresent(error -> error.printStackTrace(System.out));
            }

            if (executed >= planned) {
                printFinalSummary(executed);
            }
        }
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        if (!testIdentifier.isTest() || !isSelected(testIdentifier)) {
            return;
        }
        failed.incrementAndGet();
        int executed = completed.incrementAndGet();
        int planned = Math.max(total.get(), executed);
        int percentage = planned == 0 ? 100 : (int) Math.round(executed * 100.0 / planned);
        synchronized (System.out) {
            System.out.printf(
                    " [PROGRESO] [%s] %3d%% | Completados %d/%d | PASSED %d | FAILED %d | Pendientes %d%n",
                    progressBar(percentage), percentage, executed, planned, passed.get(), failed.get(),
                    Math.max(planned - executed, 0));
            if (detailedLogs) {
                System.out.printf(" [DETALLE]  %-9s | %s | %s%n", "FAILED",
                        singleLine(testIdentifier.getDisplayName()), singleLine(reason));
            }
            if (executed >= planned) {
                printFinalSummary(executed);
            }
        }
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        synchronized (System.out) {
            if (completed.get() > 0 && !summaryPrinted.get()) {
                printFinalSummary(completed.get());
            }
        }
    }

    private void printFinalSummary(int executed) {
        if (!summaryPrinted.compareAndSet(false, true)) {
            return;
        }
        String overallStatus = failed.get() > 0 ? "FAILED" : "PASSED";

        System.out.println(SEPARATOR);
        System.out.printf(
                " RESULTADO FINAL: %s | Ejecutados %d | PASSED %d | FAILED %d%n",
                overallStatus,
                executed,
                passed.get(),
                failed.get());
        System.out.println(MAIN_LINE);
    }

    private boolean isSelected(TestIdentifier identifier) {
        if (tagExpression == null || tagExpression.isBlank()) {
            return true;
        }
        Set<String> tags = identifier.getTags().stream()
                .map(tag -> tag.getName())
                .collect(Collectors.toUnmodifiableSet());
        return CucumberTagExpression.matches(tagExpression, tags);
    }

    private static String progressBar(int percentage) {
        int filled = (int) Math.round(percentage * BAR_WIDTH / 100.0);
        return "#".repeat(Math.min(filled, BAR_WIDTH))
                + "-".repeat(Math.max(BAR_WIDTH - filled, 0));
    }

    private static String displayStatus(TestExecutionResult.Status status) {
        return switch (status) {
            case SUCCESSFUL -> "PASSED";
            case FAILED, ABORTED -> "FAILED";
        };
    }

    private static String singleLine(String value) {
        return value == null ? "" : value.replaceAll("\\s+", " ").trim();
    }

    private static void printCentered(String title) {
        int padding = Math.max((CONSOLE_WIDTH - title.length()) / 2, 0);
        System.out.printf("%s%s%n", " ".repeat(padding), title);
    }
}
