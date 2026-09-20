package pe.com.challenge.automation.reports;

import java.nio.file.Path;
import java.util.List;

public final class ExecutionConsole {
    private static final int CONSOLE_WIDTH = 112;
    private static final String LINE = "=".repeat(CONSOLE_WIDTH);

    private ExecutionConsole() {
    }

    public static void printReportLinks(Path dashboard, List<Path> failures,
                                        int total, int passed, int failed) {
        Path absoluteDashboard = dashboard.toAbsolutePath().normalize();
        String dashboardUri = absoluteDashboard.toUri().toString();

        synchronized (System.out) {
            System.out.printf("%n%s%n", LINE);
            printCentered("QA AUTOMATION · REPORTE DE EJECUCION");
            System.out.println(LINE);
            System.out.printf(" %-18s %d   |   PASSED: %d   |   FAILED: %d%n",
                    "ESCENARIOS:", total, passed, failed);
            System.out.println("-".repeat(CONSOLE_WIDTH));
            System.out.println(" DASHBOARD GENERAL");
            System.out.println(" Local:");
            System.out.println(dashboardUri);
            printJenkinsUrl(dashboard);
            if (!failures.isEmpty()) {
                System.out.println("-".repeat(CONSOLE_WIDTH));
                System.out.println(" REPORTES TECNICOS DE ERROR");
                for (Path failure : failures) {
                    System.out.println(" " + failure.getParent().getFileName() + ":");
                    System.out.println(failure.toAbsolutePath().normalize().toUri());
                    printJenkinsUrl(failure);
                }
            }
            System.out.println(LINE);
        }
    }

    private static void printJenkinsUrl(Path report) {
        String buildUrl = System.getenv("BUILD_URL");
        if (buildUrl == null || buildUrl.isBlank()) {
            return;
        }
        try {
            Path workspace = Path.of("").toAbsolutePath().normalize();
            String artifact = workspace.relativize(report.toAbsolutePath().normalize())
                    .toString().replace('\\', '/').replace(" ", "%20");
            String separator = buildUrl.endsWith("/") ? "" : "/";
            System.out.println(" Jenkins:");
            System.out.println(buildUrl + separator + "artifact/" + artifact);
        } catch (IllegalArgumentException ignored) {
            // En unidades distintas de Windows se conserva la URL local.
        }
    }

    private static void printCentered(String title) {
        int padding = Math.max((CONSOLE_WIDTH - title.length()) / 2, 0);
        System.out.printf("%s%s%n", " ".repeat(padding), title);
    }
}
