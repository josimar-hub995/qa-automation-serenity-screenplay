package pe.com.challenge.automation.managers;

import io.cucumber.java.Scenario;
import pe.com.challenge.automation.models.DataSourceDescriptor;
import pe.com.challenge.automation.models.ApiExchange;
import pe.com.challenge.automation.models.PreparedApiRequest;
import pe.com.challenge.automation.models.ScenarioExecutionResult;
import pe.com.challenge.automation.models.ScenarioTestData;
import pe.com.challenge.automation.models.StepExecutionResult;
import pe.com.challenge.automation.exceptions.TestDataException;
import pe.com.challenge.automation.utilities.DateUtility;
import pe.com.challenge.automation.utilities.ExecutionSettings;
import pe.com.challenge.automation.utilities.FileUtility;
import pe.com.challenge.automation.utilities.HeaderSanitizer;
import pe.com.challenge.automation.utilities.JsonUtility;
import pe.com.challenge.automation.utilities.StringUtility;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class ScenarioManager {
    private static final ThreadLocal<ScenarioState> CURRENT = new ThreadLocal<>();

    private ScenarioManager() {
    }

    public static void startScenario(Scenario scenario) {
        if (!startsWithValidate(scenario.getName())) {
            throw new TestDataException(
                    "El nombre del escenario debe estar en español y comenzar con 'Validar': " + scenario.getName());
        }
        List<String> executionTags = scenario.getSourceTagNames().stream()
                .filter(tag -> tag.matches("@TC_[A-Za-z0-9_]+"))
                .toList();
        if (executionTags.size() != 1) {
            throw new TestDataException(
                    "El escenario debe declarar exactamente un TAG de ejecución @TC_<tipo>_<número>: "
                            + executionTags);
        }
        String executionTag = executionTags.get(0).substring(1);
        String folderName = executionTag + "_" + StringUtility.safeFileName(scenario.getName());
        Path path = ExecutionManager.executionPath().resolve("escenarios").resolve(folderName);
        FileUtility.createDirectories(path);
        ScenarioState state = new ScenarioState(executionTag, scenario.getName(), path, LocalDateTime.now());
        CURRENT.set(state);
        log("Inicio del escenario: " + scenario.getName());
    }

    public static void setDataset(String datasetId, Object snapshot) {
        ScenarioState state = requireState();
        state.datasetId = datasetId;
        if (snapshot instanceof ScenarioTestData testData) {
            if (!startsWithValidate(testData.scenarioName())) {
                throw new TestDataException("ESCENARIO debe comenzar con 'Validar' en el Excel");
            }
            state.testCaseId = testData.testCaseId();
            state.scenarioName = testData.scenarioName();
            state.expectedResult = testData.expectedResult();
        }
        state.dataSnapshots.put(snapshot.getClass().getSimpleName(), snapshot);
        JsonUtility.write(state.path.resolve("data/test_data.json"), state.dataSnapshots);
        log("Datos utilizados: " + datasetId);
    }

    public static void setDataSource(DataSourceDescriptor source) {
        ScenarioState state = requireState();
        state.workbookName = source.workbookName();
        state.sheetName = source.sheetName();
        state.dataSnapshots.put("dataSource", source);
        JsonUtility.write(state.path.resolve("data/test_data.json"), state.dataSnapshots);
        log("Fuente de datos: " + source.workbookName() + " / " + source.sheetName());
    }

    public static Path nextScreenshotPath(String label) {
        ScenarioState state = requireState();
        int sequence = state.screenshotSequence.incrementAndGet();
        String fileName = "%02d_%s.png".formatted(sequence, StringUtility.safeFileName(label).toLowerCase());
        return state.path.resolve("screenshots").resolve(fileName);
    }

    public static void recordScreenshot(Path screenshot) {
        ScenarioState state = requireState();
        state.screenshots.add(screenshot);
        if (state.activeStep != null) {
            state.activeStep.screenshots.add(screenshot);
        }
    }

    public static void startStep(String keyword, String text) {
        ScenarioState state = requireState();
        if (state.activeStep != null) {
            throw new IllegalStateException("Ya existe un paso Gherkin en ejecución: "
                    + state.activeStep.keyword + " " + state.activeStep.text);
        }
        state.activeStep = new StepState(
                state.steps.size() + 1,
                keyword,
                text,
                LocalDateTime.now());
        log("Inicio paso " + keyword + ": " + text);
    }

    public static void finishStep(String status, Throwable error) {
        ScenarioState state = requireState();
        StepState active = state.activeStep;
        if (active == null) {
            throw new IllegalStateException("No existe un paso Gherkin activo para finalizar");
        }

        LocalDateTime endTime = LocalDateTime.now();
        String normalizedStatus = "PASSED".equalsIgnoreCase(status) && error == null ? "PASSED" : "FAILED";
        String errorType = error == null ? "" : error.getClass().getName();
        String errorMessage = error == null ? "" : failureMessage(error);
        String stackTrace = error == null ? "" : stackTrace(error);

        StepExecutionResult result = new StepExecutionResult(
                active.sequence,
                active.keyword,
                active.text,
                normalizedStatus,
                active.startTime,
                endTime,
                Duration.between(active.startTime, endTime),
                active.screenshots,
                errorType,
                errorMessage,
                stackTrace);
        state.steps.add(result);
        state.activeStep = null;

        if (error != null) {
            registerFailure(state, result.gherkinText(), errorMessage, stackTrace);
        }
        writeStepsSnapshot(state);
        log("Fin paso " + result.gherkinText() + ". Estado: " + normalizedStatus);
    }

    public static int currentStepNumber() {
        ScenarioState state = requireState();
        return state.activeStep == null ? state.steps.size() + 1 : state.activeStep.sequence;
    }

    public static boolean isApiScenario() {
        return requireState().isApi;
    }

    public static void recordTargetUrl(String targetUrl) {
        if (targetUrl != null && !targetUrl.isBlank()) {
            requireState().targetUrl = targetUrl;
        }
    }

    public static void recordFrameworkFailure(String context, Throwable error) {
        ScenarioState state = requireState();
        String message = failureMessage(error);
        String trace = stackTrace(error);
        registerFailure(state, context, message, trace);
        log("Fallo de framework en " + context + ": " + message);
    }

    public static Path currentPath() {
        return requireState().path;
    }

    public static void recordApiRequest(PreparedApiRequest request) {
        ScenarioState state = requireState();
        state.apiMethod = request.method();
        state.apiEndpoint = request.endpoint();
        state.apiRequestHeaders = HeaderSanitizer.maskSecrets(request.headers());
        state.apiRequestParams = new LinkedHashMap<>(request.params());
        state.apiRequestBody = request.body();
        state.apiHeaderErrorConfig = request.headerErrorConfig();
        JsonUtility.write(state.path.resolve("result/api_exchange.json"), apiExchange(state));
        log("Petición API preparada: " + request.method() + " " + request.endpoint());
    }

    public static void recordApiResponse(int statusCode, String responseBody) {
        ScenarioState state = requireState();
        state.apiResponseStatus = statusCode;
        state.apiResponseBody = responseBody == null ? "" : responseBody;
        FileUtility.writeText(state.path.resolve("result/api_response.json"), state.apiResponseBody);
        JsonUtility.write(state.path.resolve("result/api_exchange.json"), apiExchange(state));
        log("Respuesta API guardada. HTTP status: " + statusCode);
    }

    public static void log(String message) {
        ScenarioState state = requireState();
        FileUtility.appendText(state.path.resolve("logs/scenario.log"),
                "[" + DateUtility.display(LocalDateTime.now()) + "] " + message + System.lineSeparator());
        if (ExecutionSettings.detailedLogsEnabled()) {
            synchronized (System.out) {
                System.out.printf(" [LOG] %-12s | %s%n", state.executionTag, message);
            }
        }
    }

    public static void finishScenario(String status) {
        ScenarioState state = requireState();

        if ("SKIPPED".equalsIgnoreCase(status)
                || "ABORTED".equalsIgnoreCase(status)) {

            log("Escenario omitido por configuración de ejecución.");
            CURRENT.remove();
            return;
        }

        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(state.startTime, endTime);

        String normalizedStatus =
                "PASSED".equalsIgnoreCase(status)
                        && state.steps.stream()
                        .noneMatch(step -> "FAILED".equalsIgnoreCase(step.status()))
                        ? "PASSED"
                        : "FAILED";

        if ("FAILED".equals(normalizedStatus)
                && state.errorMessage.isBlank()) {

            state.failedStep = "Ejecución del escenario";
            state.errorMessage =
                    "El escenario finalizó con estado FAILED. Consulte los logs y Serenity para el detalle técnico.";
        }

        if ("FAILED".equals(normalizedStatus)
                && !Files.exists(state.path.resolve("logs/error.log"))) {

            FileUtility.writeText(
                    state.path.resolve("logs/error.log"),
                    "Paso o contexto: "
                            + state.failedStep
                            + System.lineSeparator()
                            + "Error: "
                            + state.errorMessage
                            + System.lineSeparator()
                            + state.stackTrace
            );
        }

        Map<String, Object> resultJson = new LinkedHashMap<>();

        resultJson.put("tap", state.testCaseId);
        resultJson.put("tag", "@" + state.executionTag);
        resultJson.put("scenario", state.scenarioName);
        resultJson.put("expectedResult", state.expectedResult);
        resultJson.put("datos", state.datasetId);
        resultJson.put("workbook", state.workbookName);
        resultJson.put("sheet", state.sheetName);
        resultJson.put("status", normalizedStatus);
        resultJson.put("startTime", DateUtility.display(state.startTime));
        resultJson.put("endTime", DateUtility.display(endTime));
        resultJson.put(
                "durationSeconds",
                duration.toMillis() / 1000.0
        );
        resultJson.put(
                "browser",
                state.isApi
                        ? "API REST"
                        : ConfigurationManager.get("browser")
        );
        resultJson.put("environment", ConfigurationManager.environment());
        resultJson.put("evidenceCount", state.screenshots.size());
        resultJson.put("targetUrl", state.targetUrl);
        resultJson.put("failedStep", state.failedStep);
        resultJson.put("errorMessage", state.errorMessage);
        resultJson.put("stackTrace", state.stackTrace);
        resultJson.put("steps", stepMaps(state.steps));
        resultJson.put("apiExchange", apiExchange(state));

        JsonUtility.write(
                state.path.resolve("result/result.json"),
                resultJson
        );

        log(
                "Fin del escenario. Estado: "
                        + normalizedStatus
        );

        ExecutionManager.register(
                new ScenarioExecutionResult(
                        state.testCaseId,
                        state.executionTag,
                        state.scenarioName,
                        state.expectedResult,
                        state.datasetId,
                        state.workbookName,
                        state.sheetName,
                        normalizedStatus,
                        state.startTime,
                        endTime,
                        duration,
                        state.path,
                        state.isApi
                                ? "API REST"
                                : ConfigurationManager.get("browser"),
                        state.targetUrl,
                        List.copyOf(state.steps),
                        List.copyOf(state.screenshots),
                        state.failedStep,
                        state.errorMessage,
                        state.stackTrace,
                        apiExchange(state)
                )
        );

        CURRENT.remove();
    }

    private static void registerFailure(ScenarioState state, String context, String message, String trace) {
        boolean firstFailure = state.failedStep.isBlank();
        if (firstFailure) {
            state.failedStep = context;
            state.errorMessage = message;
            state.stackTrace = trace;
        }
        String entry = "Paso o contexto: " + context + System.lineSeparator()
                        + "Error: " + message + System.lineSeparator()
                        + trace + System.lineSeparator();
        if (firstFailure) {
            FileUtility.writeText(state.path.resolve("logs/error.log"), entry);
        } else {
            FileUtility.appendText(state.path.resolve("logs/error.log"),
                    System.lineSeparator() + "--- Error adicional ---" + System.lineSeparator() + entry);
        }
    }

    private static void writeStepsSnapshot(ScenarioState state) {
        JsonUtility.write(state.path.resolve("result/steps.json"), stepMaps(state.steps));
    }

    private static List<Map<String, Object>> stepMaps(List<StepExecutionResult> steps) {
        return steps.stream().map(step -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("sequence", step.sequence());
            map.put("keyword", step.keyword());
            map.put("text", step.text());
            map.put("status", step.status());
            map.put("startTime", DateUtility.stepDisplay(step.startTime()));
            map.put("endTime", DateUtility.stepDisplay(step.endTime()));
            map.put("durationSeconds", step.duration().toMillis() / 1000.0);
            map.put("screenshots", step.screenshots().stream()
                    .map(path -> path.getFileName().toString())
                    .toList());
            map.put("errorType", step.errorType());
            map.put("errorMessage", step.errorMessage());
            return map;
        }).toList();
    }

    private static String failureMessage(Throwable error) {
        String message = error.getMessage();
        return error.getClass().getSimpleName() + (message == null || message.isBlank() ? "" : ": " + message);
    }

    private static ApiExchange apiExchange(ScenarioState state) {
        if (!state.isApi) {
            return ApiExchange.empty();
        }
        return new ApiExchange(
                state.apiMethod,
                state.apiEndpoint,
                state.apiRequestHeaders,
                state.apiRequestParams,
                state.apiRequestBody,
                state.apiHeaderErrorConfig,
                state.apiResponseStatus,
                state.apiResponseBody);
    }

    private static String stackTrace(Throwable error) {
        StringWriter writer = new StringWriter();
        error.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private static ScenarioState requireState() {
        ScenarioState state = CURRENT.get();
        if (state == null) {
            throw new IllegalStateException("No existe un escenario activo");
        }
        return state;
    }

    private static boolean startsWithValidate(String text) {
        return text != null && text.stripLeading().startsWith("Validar");
    }

    private static final class ScenarioState {
        private final String executionTag;
        private final Path path;
        private final LocalDateTime startTime;
        private final AtomicInteger screenshotSequence = new AtomicInteger();
        private final List<Path> screenshots = new ArrayList<>();
        private final List<StepExecutionResult> steps = new ArrayList<>();
        private final Map<String, Object> dataSnapshots = new LinkedHashMap<>();
        private final boolean isApi;
        private StepState activeStep;
        private String testCaseId;
        private String datasetId = "N/A";
        private String scenarioName;
        private String expectedResult = "N/A";
        private String workbookName = "N/A";
        private String sheetName = "N/A";
        private String targetUrl;
        private String failedStep = "";
        private String errorMessage = "";
        private String stackTrace = "";
        private String apiMethod = "";
        private String apiEndpoint = "";
        private Map<String, String> apiRequestHeaders = Map.of();
        private Map<String, Object> apiRequestParams = Map.of();
        private String apiRequestBody = "";
        private String apiHeaderErrorConfig = "";
        private int apiResponseStatus;
        private String apiResponseBody = "";

        private ScenarioState(String executionTag, String scenarioName, Path path, LocalDateTime startTime) {
            this.executionTag = executionTag;
            this.scenarioName = scenarioName;
            this.path = path;
            this.startTime = startTime;
            this.testCaseId = tapFromExecutionTag(executionTag);
            this.isApi = executionTag.startsWith("TC_API_");
            this.targetUrl = isApi ? "N/A" : ConfigurationManager.get("base.url");
        }
    }

    private static String tapFromExecutionTag(String executionTag) {
        int separator = executionTag.lastIndexOf('_');
        String number = separator >= 0 ? executionTag.substring(separator + 1) : "";
        return number.matches("\\d{3,}") ? "TAP - " + number : executionTag;
    }

    private static final class StepState {
        private final int sequence;
        private final String keyword;
        private final String text;
        private final LocalDateTime startTime;
        private final List<Path> screenshots = new ArrayList<>();

        private StepState(int sequence, String keyword, String text, LocalDateTime startTime) {
            this.sequence = sequence;
            this.keyword = keyword;
            this.text = text;
            this.startTime = startTime;
        }
    }
}
