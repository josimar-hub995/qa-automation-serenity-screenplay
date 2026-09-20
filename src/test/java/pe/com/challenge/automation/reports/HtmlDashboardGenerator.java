package pe.com.challenge.automation.reports;

import pe.com.challenge.automation.managers.ConfigurationManager;
import pe.com.challenge.automation.managers.ExecutionManager;
import pe.com.challenge.automation.models.ApiExchange;
import pe.com.challenge.automation.models.ScenarioExecutionResult;
import pe.com.challenge.automation.utilities.DateUtility;
import pe.com.challenge.automation.utilities.FileUtility;
import pe.com.challenge.automation.utilities.JsonUtility;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class HtmlDashboardGenerator {
    private HtmlDashboardGenerator() {
    }

    public static Path generate(List<ScenarioExecutionResult> results, Path target) {
        long passed = results.stream().filter(HtmlDashboardGenerator::passed).count();
        long failed = results.size() - passed;
        long web = results.stream().filter(result -> !isApi(result)).count();
        long api = results.size() - web;
        boolean hasWeb = web > 0;
        boolean hasApi = api > 0;
        long passRate = results.isEmpty() ? 0 : Math.round(passed * 100.0 / results.size());
        double totalSeconds = results.stream().map(ScenarioExecutionResult::duration)
                .mapToDouble(Duration::toMillis).sum() / 1000.0;
        double maxSeconds = results.stream().map(ScenarioExecutionResult::duration)
                .mapToDouble(Duration::toMillis).max().orElse(1.0) / 1000.0;

        String rows = results.stream().map(HtmlDashboardGenerator::resultRows)
                .reduce("", String::concat);
        String durationBars = results.stream()
                .map(result -> durationBar(result, maxSeconds))
                .reduce("", String::concat);
        String failures = results.stream().filter(result -> !passed(result))
                .map(HtmlDashboardGenerator::failureCard)
                .reduce("", String::concat);
        if (failures.isBlank()) {
            failures = "<div class=\"empty-state\"><span>✓</span><strong>Sin fallos en esta ejecución</strong>"
                    + "<p>Todos los escenarios finalizaron con estado PASSED.</p></div>";
        }

        String html = template()
                .replace("{{REPORT}}", escape(ExecutionManager.executionPath().getFileName().toString()))
                .replace("{{ENVIRONMENT}}", escape(ConfigurationManager.environment()))
                .replace("{{START}}", escape(DateUtility.display(ExecutionManager.startedAt())))
                .replace("{{GENERATED}}", escape(DateUtility.display(LocalDateTime.now())))
                .replace("{{SCOPE_TITLE}}", scopeTitle(hasWeb, hasApi))
                .replace("{{SCOPE_SUBTITLE}}", scopeSubtitle(hasWeb, hasApi))
                .replace("{{CHANNELS}}", channelCoverage(web, api, Math.max(results.size(), 1)))
                .replace("{{TABLE_HINT}}", tableHint(hasWeb, hasApi))
                .replace("{{TOTAL}}", String.valueOf(results.size()))
                .replace("{{PASSED}}", String.valueOf(passed))
                .replace("{{FAILED}}", String.valueOf(failed))
                .replace("{{RATE}}", String.valueOf(passRate))
                .replace("{{TOTAL_SECONDS}}", decimal(totalSeconds) + " s")
                .replace("{{ROWS}}", rows)
                .replace("{{DURATION_BARS}}", durationBars)
                .replace("{{FAILURES}}", failures);
        FileUtility.writeText(target, html);
        return target;
    }

    private static String resultRows(ScenarioExecutionResult result) {
        String status = passed(result) ? "PASSED" : "FAILED";
        boolean api = isApi(result);
        String detailsId = "api-details-" + safeId(result.executionTag());
        String rowAttributes = api
                ? " class=\"scenario-row api-row\" role=\"button\" tabindex=\"0\" aria-expanded=\"false\""
                + " aria-controls=\"" + detailsId + "\" onclick=\"toggleApiDetails('"
                + detailsId + "',this)\""
                : " class=\"scenario-row\"";
        String chevron = api ? "<span class=\"chevron\" aria-hidden=\"true\">⌄</span>" : "";
        String hint = api
                ? "<span class=\"channel-badge api\">API</span> · Datos "
                + escape(result.datasetId()) + " · Clic para ver request y response"
                : "<span class=\"channel-badge web\">WEB</span> · Datos "
                + escape(result.datasetId());

        String mainRow = "<tr" + rowAttributes + ">"
                + "<td class=\"tap\"><div class=\"tap-content\"><span>"
                + escape(result.testCaseId()) + "</span>" + chevron + "</div></td>"
                + "<td><strong>" + escape(result.scenarioName()) + "</strong><small>" + hint + "</small></td>"
                + cell(result.expectedResult(), "expected")
                + "<td><span class=\"status " + status.toLowerCase(Locale.ROOT)
                + "\"><i></i>" + status + "</span></td>"
                + cell(decimal(result.duration().toMillis() / 1000.0) + " s", "duration")
                + "</tr>";

        return api ? mainRow + apiDetailsRow(result, detailsId) : mainRow;
    }

    private static String apiDetailsRow(ScenarioExecutionResult result, String detailsId) {
        ApiExchange exchange = result.apiExchange();
        if (!exchange.available()) {
            return "<tr id=\"" + detailsId + "\" class=\"api-details-row\"><td colspan=\"5\">"
                    + "<div class=\"api-empty\">No se capturó información del intercambio API. "
                    + "Revise el paso fallido y el log técnico.</div></td></tr>";
        }

        String headers = exchange.requestHeaders().isEmpty()
                ? "<div class=\"api-empty compact\">La petición no envió headers.</div>"
                : keyValueRows(exchange.requestHeaders());
        String params = exchange.requestParams().isEmpty()
                ? "<span class=\"muted\">Sin query params</span>"
                : keyValueRows(exchange.requestParams());
        String requestBody = exchange.requestBody().isBlank() || "{}".equals(exchange.requestBody().trim())
                ? "Sin request body"
                : JsonUtility.pretty(exchange.requestBody());
        String responseBody = exchange.responseBody().isBlank()
                ? "Response body vacío"
                : JsonUtility.pretty(exchange.responseBody());
        String errorConfiguration = exchange.headerErrorConfig().isBlank()
                ? "Sin mutaciones"
                : exchange.headerErrorConfig();

        return "<tr id=\"" + detailsId + "\" class=\"api-details-row\"><td colspan=\"5\">"
                + "<section class=\"api-detail\"><div class=\"api-detail-head\"><div><span>Intercambio API</span><h3>"
                + escape(exchange.method()) + " " + escape(exchange.endpoint())
                + "</h3></div><div class=\"http-status\">HTTP " + exchange.responseStatus() + "</div></div>"
                + "<div class=\"api-grid\"><article class=\"exchange-card\"><h4>Headers enviados"
                + " <small>valores sensibles protegidos</small></h4><div class=\"kv-list\">" + headers + "</div>"
                + "<div class=\"config-note\"><b>HEADER_ERROR_CONFIG</b><span>"
                + escape(errorConfiguration) + "</span></div></article>"
                + "<article class=\"exchange-card\"><h4>Parámetros</h4><div class=\"kv-list\">" + params
                + "</div><h4 class=\"subheading\">Request body</h4><pre>" + escape(requestBody)
                + "</pre></article><article class=\"exchange-card response-card\"><h4>Response body</h4><pre>"
                + escape(responseBody) + "</pre></article></div></section></td></tr>";
    }

    private static String keyValueRows(Map<?, ?> values) {
        return values.entrySet().stream()
                .map(entry -> "<div class=\"kv-row\"><code>" + escape(String.valueOf(entry.getKey()))
                        + "</code><span>" + escape(String.valueOf(entry.getValue())) + "</span></div>")
                .reduce("", String::concat);
    }

    private static String durationBar(ScenarioExecutionResult result, double maxSeconds) {
        double seconds = result.duration().toMillis() / 1000.0;
        double width = maxSeconds <= 0 ? 0 : Math.max(4, seconds * 100.0 / maxSeconds);
        return "<div class=\"duration-row\"><div class=\"duration-label\"><strong>"
                + escape(result.testCaseId()) + "</strong><span>" + decimal(seconds) + " s"
                + "</span></div><div class=\"bar-track\"><div class=\"bar-fill "
                + (passed(result) ? "bar-pass" : "bar-fail") + "\" style=\"width:"
                + String.format(Locale.US, "%.1f", width) + "%\"></div></div></div>";
    }

    private static String failureCard(ScenarioExecutionResult result) {
        String scenarioFolder = "../escenarios/" + urlSegment(result.scenarioPath().getFileName().toString()) + "/";
        return "<article class=\"failure-card\"><div class=\"failure-icon\">!</div><div><span>FAILED · "
                + escape(result.testCaseId()) + "</span><h3>" + escape(result.failedStep()) + "</h3><p>"
                + escape(result.errorMessage()) + "</p><a href=\"" + scenarioFolder
                + "error-report.html\">Abrir análisis técnico →</a></div></article>";
    }

    private static String cell(String value, String cssClass) {
        return "<td class=\"" + cssClass + "\">" + escape(value) + "</td>";
    }

    private static boolean passed(ScenarioExecutionResult result) {
        return "PASSED".equalsIgnoreCase(result.status());
    }

    private static boolean isApi(ScenarioExecutionResult result) {
        return result.executionTag().startsWith("TC_API_");
    }

    private static String scopeTitle(boolean hasWeb, boolean hasApi) {
        if (hasWeb && hasApi) {
            return "Resultados de automatización Web &amp; API";
        }
        if (hasApi) {
            return "Resultados de automatización API";
        }
        if (hasWeb) {
            return "Resultados de automatización Web";
        }
        return "Resultados de automatización";
    }

    private static String scopeSubtitle(boolean hasWeb, boolean hasApi) {
        if (hasWeb && hasApi) {
            return "Serenity BDD · Screenplay Web &amp; REST · Evidencia trazable por paso";
        }
        if (hasApi) {
            return "Serenity BDD · Screenplay REST · Evidencia técnica trazable por paso";
        }
        if (hasWeb) {
            return "Serenity BDD · Screenplay · Selenium · Evidencia visual trazable por paso";
        }
        return "Serenity BDD · Reporte de ejecución";
    }

    private static String channelCoverage(long web, long api, long total) {
        StringBuilder channels = new StringBuilder();
        if (web > 0) {
            channels.append("<div class=\"channel\"><div class=\"channel-label\"><strong>Web UI</strong><span>")
                    .append(web).append(" escenarios</span></div><div class=\"track\"><div class=\"fill\" style=\"width:")
                    .append(percentage(web, total)).append("\"></div></div></div>");
        }
        if (api > 0) {
            channels.append("<div class=\"channel\"><div class=\"channel-label\"><strong>API REST</strong><span>")
                    .append(api).append(" escenarios</span></div><div class=\"track\"><div class=\"fill api\" style=\"width:")
                    .append(percentage(api, total)).append("\"></div></div></div>");
        }
        if (channels.isEmpty()) {
            return "<div class=\"empty-state\">No se registraron escenarios ejecutados.</div>";
        }
        return channels.toString();
    }

    private static String tableHint(boolean hasWeb, boolean hasApi) {
        if (hasApi) {
            return "Las filas API se despliegan al hacer clic para mostrar request y response";
        }
        if (hasWeb) {
            return "Evidencia funcional y duración de los escenarios ejecutados";
        }
        return "Sin escenarios para mostrar";
    }

    private static String percentage(long value, long total) {
        return String.format(Locale.US, "%.1f%%", value * 100.0 / total);
    }

    private static String decimal(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static String safeId(String value) {
        return value == null ? "scenario" : value.replaceAll("[^A-Za-z0-9_-]", "-");
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
                <!doctype html>
                <html lang="es">
                <head>
                  <meta charset="utf-8">
                  <meta name="viewport" content="width=device-width,initial-scale=1">
                  <link rel="icon" type="image/png" href="assets/qa_automation_logo.png">
                  <link rel="shortcut icon" type="image/png" href="assets/qa_automation_logo.png">
                  <title>QA Executive Report · {{REPORT}}</title>
                  <style>
                    :root{--navy:#0b2347;--blue:#155eef;--cyan:#0ea5e9;--purple:#7c3aed;--ink:#172033;--muted:#667085;--line:#dce3ef;--bg:#f5f7fb;--card:#fff;--pass:#12a150;--pass-soft:#eaf8f0;--fail:#d92d3a;--fail-soft:#fff0f1;--shadow:0 14px 40px rgba(15,35,70,.08)}
                    *{box-sizing:border-box}html{scroll-behavior:smooth}body{margin:0;background:var(--bg);color:var(--ink);font-family:Inter,"Segoe UI",Arial,sans-serif}a{color:inherit}.shell{max-width:1500px;margin:auto;padding:30px}
                    .hero{position:relative;overflow:hidden;background:linear-gradient(125deg,#071b37 0%,#103d79 62%,#155eef 100%);border-radius:24px;padding:34px 38px;color:#fff;box-shadow:var(--shadow)}.hero:after{content:"";position:absolute;width:360px;height:360px;border:70px solid rgba(255,255,255,.07);border-radius:50%;right:-90px;top:-180px}.brand{position:relative;z-index:1;display:flex;align-items:center;gap:22px}.brand img{width:86px;height:86px;background:#fff;border-radius:20px;padding:8px;object-fit:contain}.eyebrow{display:block;color:#a9cbff;font-size:12px;font-weight:800;letter-spacing:.16em;text-transform:uppercase}.hero h1{font-size:32px;margin:7px 0 9px}.hero p{margin:0;color:#dbe9ff}.run-id{margin-left:auto;text-align:right}.run-id strong{font-size:16px}.run-id span{display:block;color:#b9d2f8;margin-top:6px;font-size:13px}
                    .metrics{display:grid;grid-template-columns:repeat(5,minmax(150px,1fr));gap:16px;margin:22px 0}.metric,.panel{background:var(--card);border:1px solid var(--line);border-radius:18px;box-shadow:0 7px 25px rgba(15,35,70,.04)}.metric{padding:20px}.metric .label{font-size:12px;text-transform:uppercase;letter-spacing:.08em;color:var(--muted);font-weight:800}.metric .value{display:flex;align-items:end;gap:8px;margin-top:10px;font-size:32px;font-weight:800;color:var(--navy)}.metric .hint{font-size:12px;color:var(--muted);font-weight:500;margin-bottom:4px}.metric.pass{border-top:4px solid var(--pass)}.metric.fail{border-top:4px solid var(--fail)}
                    .analytics{display:grid;grid-template-columns:340px 1fr;gap:18px;margin-bottom:22px}.panel{padding:24px}.panel-title{display:flex;align-items:center;justify-content:space-between;margin-bottom:20px}.panel-title h2{font-size:18px;color:var(--navy);margin:0}.panel-title span{font-size:12px;color:var(--muted)}.donut-wrap{display:flex;align-items:center;justify-content:center;gap:25px}.donut{width:176px;height:176px;border-radius:50%;background:conic-gradient(var(--pass) 0 {{RATE}}%,var(--fail) {{RATE}}% 100%);display:grid;place-items:center}.donut:before{content:"";width:118px;height:118px;border-radius:50%;background:#fff}.donut-label{position:absolute;text-align:center}.donut-label strong{display:block;font-size:32px;color:var(--navy)}.donut-label span{font-size:12px;color:var(--muted)}.legend div{display:flex;align-items:center;gap:8px;margin:12px 0}.dot{width:10px;height:10px;border-radius:50%}.dot.pass{background:var(--pass)}.dot.fail{background:var(--fail)}
                    .channel{margin:17px 0}.channel-label{display:flex;justify-content:space-between;font-size:13px;margin-bottom:7px}.channel-label strong{color:var(--navy)}.track,.bar-track{height:10px;border-radius:999px;background:#edf1f7;overflow:hidden}.fill{height:100%;border-radius:inherit;background:linear-gradient(90deg,var(--blue),var(--cyan))}.fill.api{background:linear-gradient(90deg,var(--purple),#b05cff)}.duration-grid{display:grid;grid-template-columns:repeat(2,minmax(250px,1fr));gap:13px 28px}.duration-label{display:flex;justify-content:space-between;font-size:12px;margin-bottom:6px}.duration-label strong{color:var(--navy)}.bar-fill{height:100%;border-radius:inherit}.bar-pass{background:linear-gradient(90deg,#12a150,#46ce82)}.bar-fail{background:linear-gradient(90deg,#d92d3a,#ff6671)}
                    .table-panel{padding:0;overflow:hidden;margin-bottom:22px}.table-heading{padding:23px 25px;display:flex;justify-content:space-between;align-items:center;border-bottom:1px solid var(--line)}.table-heading h2{margin:0;color:var(--navy);font-size:19px}.table-heading span{color:var(--muted);font-size:12px}.table-scroll{overflow:auto}table{width:100%;min-width:980px;border-collapse:collapse}th{background:#102e59;color:#fff;font-size:11px;letter-spacing:.07em;text-transform:uppercase;text-align:left;padding:14px 15px}td{padding:15px;border-bottom:1px solid #e8edf4;font-size:12.5px;line-height:1.45;vertical-align:top}.scenario-row:hover{background:#f8fbff}.tap{font-weight:800;color:var(--navy);white-space:nowrap}.tap-content{display:flex;align-items:center;justify-content:space-between;gap:12px}.chevron{display:grid;place-items:center;width:25px;height:25px;border-radius:8px;background:#f0eafe;color:var(--purple);font-size:18px;transition:transform .2s ease}.api-row{cursor:pointer}.api-row:focus{outline:3px solid rgba(21,94,239,.25);outline-offset:-3px}.api-row.expanded{background:#f8f6ff}.api-row.expanded .chevron{transform:rotate(180deg)}.expected{max-width:370px}td small{display:block;color:var(--muted);margin-top:6px}.channel-badge{font-size:10px;font-weight:900;letter-spacing:.04em}.channel-badge.api{color:var(--purple)}.channel-badge.web{color:var(--blue)}.status{display:inline-flex;align-items:center;gap:7px;border-radius:999px;padding:6px 10px;font-size:11px;font-weight:900}.status i{width:7px;height:7px;border-radius:50%;background:currentColor}.status.passed{color:var(--pass);background:var(--pass-soft)}.status.failed{color:var(--fail);background:var(--fail-soft)}.duration{white-space:nowrap;font-weight:700}
                    .api-details-row{display:none}.api-details-row.open{display:table-row}.api-details-row>td{padding:0;background:#f3f0ff;border-bottom:2px solid #cfc4f7}.api-detail{padding:22px 24px 25px;border-left:4px solid var(--purple)}.api-detail-head{display:flex;align-items:center;justify-content:space-between;gap:18px;margin-bottom:16px}.api-detail-head span{display:block;text-transform:uppercase;letter-spacing:.12em;font-size:10px;font-weight:900;color:var(--purple)}.api-detail-head h3{margin:5px 0 0;font:600 13px/1.45 Consolas,"SFMono-Regular",monospace;color:var(--navy);overflow-wrap:anywhere}.http-status{white-space:nowrap;border-radius:10px;background:#fff;border:1px solid #d9d2f4;padding:9px 12px;font-weight:900;color:var(--purple)}.api-grid{display:grid;grid-template-columns:.9fr .9fr 1.2fr;gap:14px}.exchange-card{min-width:0;background:#fff;border:1px solid #ddd7f4;border-radius:14px;padding:16px;box-shadow:0 5px 15px rgba(65,41,125,.04)}.exchange-card h4{margin:0 0 12px;color:var(--navy);font-size:12px;text-transform:uppercase;letter-spacing:.06em}.exchange-card h4 small{display:block;color:var(--muted);font-size:9px;text-transform:none;letter-spacing:0;margin-top:3px;font-weight:500}.subheading{margin-top:18px!important}.kv-list{display:grid;gap:7px}.kv-row{display:grid;grid-template-columns:minmax(105px,.8fr) minmax(0,1.2fr);gap:10px;padding:7px 0;border-bottom:1px solid #eef0f5}.kv-row:last-child{border-bottom:0}.kv-row code{color:#5b32bd;font-size:11px;overflow-wrap:anywhere}.kv-row span{color:#344054;font-size:11px;overflow-wrap:anywhere}.exchange-card pre{margin:0;background:#101828;color:#d7e3f5;border-radius:10px;padding:13px;max-height:300px;overflow:auto;white-space:pre-wrap;overflow-wrap:anywhere;font:11px/1.5 Consolas,"SFMono-Regular",monospace}.response-card pre{min-height:190px}.config-note{display:grid;gap:4px;margin-top:14px;padding:10px;border-radius:9px;background:#f8f6ff}.config-note b{font-size:9px;color:var(--purple);letter-spacing:.05em}.config-note span{font:10px/1.4 Consolas,monospace;overflow-wrap:anywhere}.api-empty{padding:22px;text-align:center;color:var(--muted);border:1px dashed #c9c0ed;border-radius:12px}.api-empty.compact{padding:10px}.muted{color:var(--muted);font-size:11px}
                    .failure-card{display:grid;grid-template-columns:44px 1fr;gap:14px;padding:17px;border:1px solid #ffc8cc;background:#fff8f8;border-radius:14px;margin:12px 0}.failure-icon{width:38px;height:38px;border-radius:11px;background:var(--fail);color:#fff;display:grid;place-items:center;font-weight:900}.failure-card span{color:var(--fail);font-size:11px;font-weight:900}.failure-card h3{margin:4px 0;color:var(--navy);font-size:15px}.failure-card p{margin:5px 0 10px;color:#475467;font-size:13px}.failure-card a{color:var(--fail);font-size:12px;font-weight:900;text-decoration:none}.empty-state{text-align:center;padding:30px;color:var(--muted)}.empty-state span{display:grid;place-items:center;width:48px;height:48px;margin:auto;border-radius:50%;background:var(--pass-soft);color:var(--pass);font-size:24px}.empty-state strong{display:block;color:var(--navy);margin-top:10px}.empty-state p{margin:5px}.footer{display:flex;justify-content:space-between;color:var(--muted);font-size:11px;padding:7px 4px 20px}
                    @media(max-width:1050px){.metrics{grid-template-columns:repeat(2,1fr)}.analytics{grid-template-columns:1fr}.duration-grid{grid-template-columns:1fr}.run-id{display:none}.api-grid{grid-template-columns:1fr 1fr}.response-card{grid-column:1/-1}}@media(max-width:680px){.shell{padding:12px}.hero{padding:24px}.brand{align-items:flex-start}.brand img{width:62px;height:62px}.hero h1{font-size:23px}.metrics{grid-template-columns:1fr 1fr}.metric{padding:16px}.metric .value{font-size:25px}.donut-wrap{flex-direction:column}.table-heading{align-items:flex-start;gap:8px;flex-direction:column}.api-grid{grid-template-columns:1fr}.response-card{grid-column:auto}.api-detail-head{align-items:flex-start;flex-direction:column}.footer{display:block}.footer span{display:block;margin:5px}}
                  </style>
                </head>
                <body><main class="shell">
                  <header class="hero"><div class="brand"><img src="assets/qa_automation_logo.png" alt="QA Automation"><div><span class="eyebrow">Quality Engineering · Executive report</span><h1>{{SCOPE_TITLE}}</h1><p>{{SCOPE_SUBTITLE}}</p></div><div class="run-id"><strong>{{REPORT}}</strong><span>Ambiente {{ENVIRONMENT}} · {{START}}</span></div></div></header>
                  <section class="metrics"><article class="metric"><span class="label">Escenarios</span><div class="value">{{TOTAL}}<span class="hint">ejecutados</span></div></article><article class="metric pass"><span class="label">PASSED</span><div class="value">{{PASSED}}<span class="hint">casos</span></div></article><article class="metric fail"><span class="label">FAILED</span><div class="value">{{FAILED}}<span class="hint">casos</span></div></article><article class="metric"><span class="label">Pass rate</span><div class="value">{{RATE}}%<span class="hint">cumplimiento</span></div></article><article class="metric"><span class="label">Duración acumulada</span><div class="value" style="font-size:26px">{{TOTAL_SECONDS}}</div></article></section>
                  <section class="analytics"><article class="panel"><div class="panel-title"><h2>Estado global</h2><span>PASSED / FAILED</span></div><div class="donut-wrap"><div style="position:relative"><div class="donut"></div><div class="donut-label" style="inset:55px 0 auto"><strong>{{RATE}}%</strong><span>pass rate</span></div></div><div class="legend"><div><i class="dot pass"></i><strong>{{PASSED}}</strong> PASSED</div><div><i class="dot fail"></i><strong>{{FAILED}}</strong> FAILED</div></div></div></article><article class="panel"><div class="panel-title"><h2>Cobertura y duración</h2><span>distribución de la ejecución</span></div>{{CHANNELS}}<div class="duration-grid" style="margin-top:24px">{{DURATION_BARS}}</div></article></section>
                  <section class="panel table-panel"><div class="table-heading"><h2>Detalle de escenarios</h2><span>{{TABLE_HINT}}</span></div><div class="table-scroll"><table><thead><tr><th>TAP</th><th>Escenario</th><th>Resultado esperado</th><th>Estado</th><th>Duración</th></tr></thead><tbody>{{ROWS}}</tbody></table></div></section>
                  <section class="panel"><div class="panel-title"><h2>Análisis de fallos</h2><span>paso, causa y evidencia final</span></div>{{FAILURES}}</section>
                  <footer class="footer"><span>Generado {{GENERATED}} · Ambiente {{ENVIRONMENT}}</span><span>Los estados de negocio se normalizan exclusivamente como PASSED o FAILED.</span></footer>
                </main><script>
                  function toggleApiDetails(id,row){const detail=document.getElementById(id);if(!detail)return;const open=detail.classList.toggle('open');row.classList.toggle('expanded',open);row.setAttribute('aria-expanded',String(open));}
                  document.addEventListener('keydown',event=>{if((event.key==='Enter'||event.key===' ')&&event.target.classList.contains('api-row')){event.preventDefault();event.target.click();}});
                </script></body></html>
                """;
    }
}
