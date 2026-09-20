package pe.com.challenge.automation.constants;

import java.nio.file.Path;

public final class FrameworkConstants {
    public static final Path REPORT_ROOT = Path.of("reports");
    public static final String EXECUTION_PREFIX = "REPORT";
    public static final String EXECUTION_POINTER = "target/current-execution.properties";
    public static final String LOGO_RESOURCE = "assets/logo/qa_automation_logo.png";

    private FrameworkConstants() {
    }
}
