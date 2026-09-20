package pe.com.challenge.automation.reports;

import java.nio.file.Path;
import java.util.List;

public final class ExecutionConsole {

    private static final int CONSOLE_WIDTH = 112;
    private static final String LINE = "=".repeat(CONSOLE_WIDTH);
    private static final String SEPARATOR = "-".repeat(CONSOLE_WIDTH);

    private ExecutionConsole() {
    }

    public static void printReportLinks(
            Path dashboard,
            Path serenityReport,
            Path archivedSerenityReport,
            List<Path> failures,
            int total,
            int passed,
            int failed) {

        Path absoluteDashboard =
                dashboard.toAbsolutePath().normalize();

        Path absoluteSerenityReport =
                serenityReport.toAbsolutePath().normalize();

        Path absoluteArchivedSerenityReport =
                archivedSerenityReport.toAbsolutePath().normalize();

        synchronized (System.out) {

            System.out.printf("%n%s%n", LINE);

            printCentered(
                    "QA AUTOMATION · REPORTE DE EJECUCION"
            );

            System.out.println(LINE);

            System.out.printf(
                    " %-18s %d   |   PASSED: %d   |   FAILED: %d%n",
                    "ESCENARIOS:",
                    total,
                    passed,
                    failed
            );

            System.out.println(SEPARATOR);

            System.out.println(" DASHBOARD GENERAL");
            System.out.println(" Local:");
            System.out.println(
                    absoluteDashboard.toUri()
            );

            printJenkinsUrl(
                    absoluteDashboard
            );

            System.out.println(SEPARATOR);

            System.out.println(" REPORTE SERENITY BDD");
            System.out.println(" Local:");
            System.out.println(
                    absoluteSerenityReport.toUri()
            );

            System.out.println(" Archivado:");
            System.out.println(
                    absoluteArchivedSerenityReport.toUri()
            );

            printJenkinsUrl(
                    absoluteArchivedSerenityReport
            );

            if (!failures.isEmpty()) {

                System.out.println(SEPARATOR);
                System.out.println(
                        " REPORTES TECNICOS DE ERROR"
                );

                for (Path failure : failures) {

                    Path absoluteFailure =
                            failure.toAbsolutePath()
                                    .normalize();

                    Path parent =
                            absoluteFailure.getParent();

                    String scenarioName =
                            parent == null
                                    ? absoluteFailure
                                    .getFileName()
                                    .toString()
                                    : parent
                                    .getFileName()
                                    .toString();

                    System.out.println(
                            " "
                                    + scenarioName
                                    + ":"
                    );

                    System.out.println(
                            absoluteFailure.toUri()
                    );

                    printJenkinsUrl(
                            absoluteFailure
                    );
                }
            }

            System.out.println(LINE);
        }
    }

    private static void printJenkinsUrl(
            Path report) {

        String buildUrl =
                System.getenv("BUILD_URL");

        if (buildUrl == null
                || buildUrl.isBlank()) {

            return;
        }

        try {

            Path workspace =
                    Path.of("")
                            .toAbsolutePath()
                            .normalize();

            String artifact =
                    workspace.relativize(
                                    report
                                            .toAbsolutePath()
                                            .normalize()
                            )
                            .toString()
                            .replace('\\', '/')
                            .replace(" ", "%20");

            String separator =
                    buildUrl.endsWith("/")
                            ? ""
                            : "/";

            System.out.println(" Jenkins:");

            System.out.println(
                    buildUrl
                            + separator
                            + "artifact/"
                            + artifact
            );

        } catch (IllegalArgumentException ignored) {
        }
    }

    private static void printCentered(
            String title) {

        int padding =
                Math.max(
                        (CONSOLE_WIDTH
                                - title.length())
                                / 2,
                        0
                );

        System.out.printf(
                "%s%s%n",
                " ".repeat(padding),
                title
        );
    }
}