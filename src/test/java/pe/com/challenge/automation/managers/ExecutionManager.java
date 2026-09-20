package pe.com.challenge.automation.managers;

import pe.com.challenge.automation.constants.FrameworkConstants;
import pe.com.challenge.automation.models.ScenarioExecutionResult;
import pe.com.challenge.automation.utilities.DateUtility;
import pe.com.challenge.automation.utilities.DirectoryUtility;
import pe.com.challenge.automation.utilities.FileUtility;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ExecutionManager {
    private static final Object LOCK = new Object();
    private static final Queue<ScenarioExecutionResult> RESULTS = new ConcurrentLinkedQueue<>();
    private static volatile Path executionPath;
    private static volatile int executionNumber;
    private static volatile LocalDateTime startedAt;

    private ExecutionManager() {
    }

    public static Path startExecution() {
        if (executionPath == null) {
            synchronized (LOCK) {
                if (executionPath == null) {
                    FileUtility.createDirectories(FrameworkConstants.REPORT_ROOT);
                    executionNumber = DirectoryUtility.nextExecutionNumber(
                            FrameworkConstants.REPORT_ROOT,
                            FrameworkConstants.EXECUTION_PREFIX);
                    startedAt = LocalDateTime.now();
                    String folderName = "%s_%03d_%s".formatted(
                            FrameworkConstants.EXECUTION_PREFIX,
                            executionNumber,
                            DateUtility.folderTimestamp());
                    executionPath = FileUtility.createDirectories(FrameworkConstants.REPORT_ROOT.resolve(folderName))
                            .toAbsolutePath().normalize();
                    FileUtility.createDirectories(executionPath.resolve("resumen"));
                    FileUtility.createDirectories(executionPath.resolve("serenity"));
                    FileUtility.createDirectories(executionPath.resolve("word"));
                    FileUtility.createDirectories(executionPath.resolve("escenarios"));
                    String antPath = executionPath.toString().replace('\\', '/');
                    FileUtility.writeText(Path.of(FrameworkConstants.EXECUTION_POINTER),
                            "execution.path=" + antPath + System.lineSeparator()
                                    + "serenity.archive.enabled=true" + System.lineSeparator());
                }
            }
        }
        return executionPath;
    }

    public static Path executionPath() {
        return startExecution();
    }

    public static int executionNumber() {
        startExecution();
        return executionNumber;
    }

    public static LocalDateTime startedAt() {
        startExecution();
        return startedAt;
    }

    public static void register(ScenarioExecutionResult result) {
        RESULTS.add(result);
    }

    public static List<ScenarioExecutionResult> results() {
        return RESULTS.stream()
                .sorted(Comparator.comparing(ScenarioExecutionResult::startTime))
                .toList();
    }
}
