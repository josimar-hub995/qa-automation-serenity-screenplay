package pe.com.challenge.automation.reports;

import pe.com.challenge.automation.managers.ConfigurationManager;
import pe.com.challenge.automation.managers.ExecutionManager;
import pe.com.challenge.automation.models.ScenarioExecutionResult;
import pe.com.challenge.automation.models.StepExecutionResult;
import pe.com.challenge.automation.utilities.DateUtility;
import pe.com.challenge.automation.utilities.FileUtility;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class ScenarioErrorReportGenerator {
    private ScenarioErrorReportGenerator() {
    }

    public static Path generate(ScenarioExecutionResult result, Path target) {
        String steps = result.steps().isEmpty()
                ? "<div class=\"empty\">No se registraron pasos Gherkin; el fallo ocurrió en la preparación del escenario.</div>"
                : result.steps().stream().map(ScenarioErrorReportGenerator::stepCard)
                .reduce("", String::concat);
        String lastEvidence = lastEvidence(result.screenshots());
        String apiResponse = Files.exists(result.scenarioPath().resolve("result/api_exchange.json"))
                ? "<a class=\"file-link\" href=\"result/api_exchange.json\">Request / response API</a>"
                : "";

        String html = template()
                .replace("{{TAP}}", escape(result.testCaseId()))
                .replace("{{TAG}}", escape("@" + result.executionTag()))
                .replace("{{WORD}}", urlSegment(ReportNaming.wordFileName(result)))
                .replace("{{SCENARIO}}", escape(result.scenarioName()))
                .replace("{{REPORT}}", escape(ExecutionManager.executionPath().getFileName().toString()))
                .replace("{{ENVIRONMENT}}", escape(ConfigurationManager.environment()))
                .replace("{{BROWSER}}", escape(result.browser()))
                .replace("{{URL}}", escape(result.targetUrl()))
                .replace("{{SOURCE}}", escape(result.workbookName() + " / " + result.sheetName()
                        + " / Datos " + result.datasetId()))
                .replace("{{START}}", escape(DateUtility.stepDisplay(result.startTime())))
                .replace("{{DURATION}}", decimal(result.duration().toMillis() / 1000.0) + " s")
                .replace("{{FAILED_STEP}}", escape(defaultText(result.failedStep(), "Ejecución del escenario")))
                .replace("{{ERROR}}", escape(defaultText(result.errorMessage(), "Fallo sin mensaje técnico disponible")))
                .replace("{{STACK}}", escape(defaultText(result.stackTrace(), "Stack trace no disponible")))
                .replace("{{STEPS}}", steps)
                .replace("{{LAST_EVIDENCE}}", lastEvidence)
                .replace("{{API_RESPONSE}}", apiResponse);
        FileUtility.writeText(target, html);
        return target;
    }

    private static String stepCard(StepExecutionResult step) {
        String status = "PASSED".equalsIgnoreCase(step.status()) ? "PASSED" : "FAILED";
        String screenshots = step.screenshots().stream()
                .map(path -> "<figure><img src=\"screenshots/" + urlSegment(path.getFileName().toString())
                        + "\" alt=\"Evidencia del paso " + step.sequence()
                        + "\"><figcaption>Captura automática · paso " + step.sequence() + "</figcaption></figure>")
                .reduce("", String::concat);
        String failure = "FAILED".equals(status)
                ? "<div class=\"step-error\"><strong>Causa registrada</strong><p>"
                + escape(step.errorMessage()) + "</p></div>"
                : "";
        return "<article class=\"step-card " + status.toLowerCase() + "\"><div class=\"step-marker\">"
                + step.sequence() + "</div><div class=\"step-content\"><div class=\"step-head\"><time>"
                + escape(DateUtility.stepDisplay(step.startTime())) + "</time><span class=\"badge "
                + status.toLowerCase() + "\">" + status + "</span></div><h3>"
                + escape(step.gherkinText()) + "</h3><div class=\"timing\"><span>Duración</span><div><i style=\"width:"
                + stepWidth(step) + "\"></i></div><b>" + decimal(step.duration().toMillis() / 1000.0)
                + " s</b></div>" + failure + screenshots + "</div></article>";
    }

    private static String lastEvidence(List<Path> screenshots) {
        if (screenshots.isEmpty()) {
            return "<div class=\"empty\">No fue posible obtener una captura final; revise el stack trace y los logs.</div>";
        }
        Path last = screenshots.get(screenshots.size() - 1);
        return "<figure class=\"final-shot\"><img src=\"screenshots/"
                + urlSegment(last.getFileName().toString())
                + "\" alt=\"Último estado capturado\"><figcaption>Último estado capturado antes de finalizar el escenario</figcaption></figure>";
    }

    private static String stepWidth(StepExecutionResult step) {
        double seconds = Math.max(.05, step.duration().toMillis() / 1000.0);
        return String.format(Locale.US, "%.1f%%", Math.min(100, 18 + Math.log1p(seconds) * 28));
    }

    private static String decimal(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String urlSegment(String value) {
        return value.replace("%", "%25").replace(" ", "%20").replace("#", "%23");
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String template() {
        return """
                <!doctype html><html lang="es"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>FAILED · {{TAP}}</title>
                <style>
                :root{--navy:#0b2347;--blue:#155eef;--ink:#172033;--muted:#667085;--line:#dce3ef;--bg:#f4f6fa;--pass:#12a150;--fail:#d92d3a;--fail-soft:#fff1f2}*{box-sizing:border-box}body{margin:0;background:var(--bg);font-family:Inter,"Segoe UI",Arial,sans-serif;color:var(--ink)}a{color:inherit}.shell{max-width:1220px;margin:auto;padding:28px}.top{background:linear-gradient(125deg,#3c0b13,#8e1826 58%,#d92d3a);color:#fff;border-radius:22px;padding:30px 34px;box-shadow:0 16px 42px rgba(83,13,24,.17)}.brand{display:flex;align-items:center;gap:18px}.brand img{width:72px;height:72px;object-fit:contain;background:#fff;border-radius:16px;padding:7px}.eyebrow{color:#ffd4d7;text-transform:uppercase;letter-spacing:.14em;font-size:11px;font-weight:900}.top h1{margin:6px 0;font-size:28px}.top p{margin:0;color:#ffe2e4}.status-big{margin-left:auto;background:#fff;color:var(--fail);border-radius:999px;padding:11px 16px;font-weight:900;font-size:13px}.nav{display:flex;gap:9px;margin:18px 0}.nav a,.file-link{background:#fff;border:1px solid var(--line);border-radius:9px;padding:9px 12px;text-decoration:none;color:var(--navy);font-size:12px;font-weight:800}.grid{display:grid;grid-template-columns:1.05fr .95fr;gap:18px}.panel{background:#fff;border:1px solid var(--line);border-radius:17px;padding:22px;box-shadow:0 7px 24px rgba(11,35,71,.05)}.panel h2{font-size:17px;color:var(--navy);margin:0 0 16px}.failure-summary{border-left:5px solid var(--fail);background:var(--fail-soft)}.failure-summary .where{font-size:11px;text-transform:uppercase;color:var(--fail);letter-spacing:.1em;font-weight:900}.failure-summary h2{margin:7px 0 9px;font-size:20px}.failure-summary p{line-height:1.55;color:#5c2930}.meta{display:grid;grid-template-columns:145px 1fr;gap:0}.meta div{padding:9px;border-bottom:1px solid #edf0f5;font-size:12px;overflow-wrap:anywhere}.meta div:nth-child(odd){font-weight:800;color:#344054}.steps{margin-top:20px}.steps-title{display:flex;justify-content:space-between;align-items:end;margin-bottom:13px}.steps-title h2{margin:0;color:var(--navy)}.steps-title span{font-size:12px;color:var(--muted)}.step-card{position:relative;display:grid;grid-template-columns:42px 1fr;gap:13px;background:#fff;border:1px solid var(--line);border-radius:16px;padding:18px;margin:12px 0}.step-card.failed{border-color:#ffb6bc;box-shadow:0 8px 25px rgba(217,45,58,.08)}.step-marker{width:34px;height:34px;border-radius:11px;background:#eaf8f0;color:var(--pass);display:grid;place-items:center;font-weight:900}.step-card.failed .step-marker{background:var(--fail-soft);color:var(--fail)}.step-head{display:flex;justify-content:space-between;gap:10px}.step-head time{font-size:11px;color:var(--muted);font-weight:700}.badge{border-radius:999px;padding:5px 9px;font-size:10px;font-weight:900}.badge.passed{color:var(--pass);background:#eaf8f0}.badge.failed{color:var(--fail);background:var(--fail-soft)}.step-card h3{font-size:15px;color:var(--navy);margin:8px 0 13px}.timing{display:grid;grid-template-columns:55px 1fr 60px;align-items:center;gap:10px;font-size:10px;color:var(--muted)}.timing div{height:7px;background:#edf1f6;border-radius:999px;overflow:hidden}.timing i{display:block;height:100%;background:linear-gradient(90deg,var(--blue),#53a1ff);border-radius:inherit}.failed .timing i{background:linear-gradient(90deg,var(--fail),#ff7780)}.timing b{text-align:right;color:#344054}.step-error{margin-top:13px;background:var(--fail-soft);border-radius:10px;padding:12px;color:#6e2029}.step-error strong{font-size:11px;text-transform:uppercase}.step-error p{font-size:12px;margin:5px 0}.step-card figure,.final-shot{margin:16px 0 0;text-align:center}.step-card img,.final-shot img{display:block;max-width:100%;height:auto;margin:auto;border:1px solid #d8dee8;border-radius:12px;box-shadow:0 8px 24px rgba(15,35,70,.1)}figcaption{font-size:10px;color:var(--muted);margin-top:7px}.technical{margin-top:18px}.technical pre{white-space:pre-wrap;overflow-wrap:anywhere;background:#101828;color:#d0d5dd;border-radius:12px;padding:17px;max-height:380px;overflow:auto;font:11px/1.5 Consolas,monospace}.empty{text-align:center;color:var(--muted);border:1px dashed #c7cfdb;border-radius:12px;padding:25px}.footer{text-align:center;color:var(--muted);font-size:11px;padding:24px}@media(max-width:800px){.shell{padding:12px}.grid{grid-template-columns:1fr}.status-big{display:none}.meta{grid-template-columns:110px 1fr}.top{padding:24px}.top h1{font-size:22px}}
                </style></head><body><main class="shell">
                <header class="top"><div class="brand"><img src="../../resumen/assets/qa_automation_logo.png" alt="QA"><div><span class="eyebrow">Failure analysis · {{REPORT}}</span><h1>{{TAP}} · {{SCENARIO}}</h1><p>{{TAG}} · Ambiente {{ENVIRONMENT}}</p></div><span class="status-big">FAILED</span></div></header>
                <nav class="nav"><a href="../../resumen/dashboard.html">← Dashboard</a><a href="../../word/{{WORD}}">Reporte Word</a><a href="logs/error.log">Log de error</a>{{API_RESPONSE}}</nav>
                <section class="grid"><article class="panel failure-summary"><span class="where">La automatización se detuvo en</span><h2>{{FAILED_STEP}}</h2><p>{{ERROR}}</p></article><article class="panel"><h2>Contexto de ejecución</h2><div class="meta"><div>TAP</div><div>{{TAP}}</div><div>Canal</div><div>{{BROWSER}}</div><div>Página / servicio</div><div>{{URL}}</div><div>Datos</div><div>{{SOURCE}}</div><div>Inicio</div><div>{{START}}</div><div>Duración</div><div>{{DURATION}}</div></div></article></section>
                <section class="steps"><div class="steps-title"><h2>Línea de ejecución Gherkin</h2><span>tiempo, estado, evidencia y causa por paso</span></div>{{STEPS}}</section>
                <section class="panel technical"><h2>Última evidencia disponible</h2>{{LAST_EVIDENCE}}</section>
                <section class="panel technical"><h2>Stack trace técnico</h2><pre>{{STACK}}</pre></section>
                <footer class="footer">Reporte técnico generado automáticamente · Estados normalizados: PASSED / FAILED</footer>
                </main></body></html>
                """;
    }
}
