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

        Path absoluteSerenity =
                serenityReport.toAbsolutePath().normalize();

        Path absoluteArchivedSerenity =
                archivedSerenityReport.toAbsolutePath().normalize();

        synchronized (System.out) {

            System.out.printf("%n%s%n", LINE);
            printCentered("QA AUTOMATION · REPORTE DE EJECUCION");
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
            System.out.println(absoluteDashboard.toUri());

            printPipelineLinks();

            System.out.println(SEPARATOR);

            System.out.println(" REPORTE SERENITY BDD");
            System.out.println(" Local:");
            System.out.println(absoluteSerenity.toUri());

            System.out.println(" Archivado:");
            System.out.println(absoluteArchivedSerenity.toUri());

            printPipelineLinks();

            if (!failures.isEmpty()) {

                System.out.println(SEPARATOR);
                System.out.println(" REPORTES TECNICOS DE ERROR");

                for (Path failure : failures) {

                    Path absoluteFailure =
                            failure.toAbsolutePath().normalize();

                    Path parent =
                            absoluteFailure.getParent();

                    String scenarioName =
                            parent == null
                                    ? absoluteFailure.getFileName().toString()
                                    : parent.getFileName().toString();

                    System.out.println(" " + scenarioName + ":");
                    System.out.println(absoluteFailure.toUri());
                }
            }

            System.out.println(LINE);
        }
    }

    private static void printPipelineLinks() {

        String githubServer =
                System.getenv("GITHUB_SERVER_URL");

        String githubRepository =
                System.getenv("GITHUB_REPOSITORY");

        String githubRunId =
                System.getenv("GITHUB_RUN_ID");

        if (githubServer != null
                && !githubServer.isBlank()
                && githubRepository != null
                && !githubRepository.isBlank()
                && githubRunId != null
                && !githubRunId.isBlank()) {

            String runUrl =
                    githubServer
                            + "/"
                            + githubRepository
                            + "/actions/runs/"
                            + githubRunId;

            System.out.println(" GitHub Actions:");
            System.out.println(runUrl);

            System.out.println(" Artifacts:");
            System.out.println(runUrl + "#artifacts");

            return;
        }

        String buildUrl =
                System.getenv("BUILD_URL");

        if (buildUrl != null
                && !buildUrl.isBlank()) {

            System.out.println(" Jenkins:");
            System.out.println(buildUrl);
        }
    }

    private static void printCentered(String title) {

        int padding =
                Math.max(
                        (CONSOLE_WIDTH - title.length()) / 2,
                        0
                );

        System.out.printf(
                "%s%s%n",
                " ".repeat(padding),
                title
        );
    }
}