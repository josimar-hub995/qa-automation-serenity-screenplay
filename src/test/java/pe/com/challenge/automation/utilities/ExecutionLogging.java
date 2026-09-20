package pe.com.challenge.automation.utilities;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/** Configura los logs Java/Serenity según execution.logs. */
public final class ExecutionLogging {
    private ExecutionLogging() {
    }

    public static void configure() {
        boolean detailed = ExecutionSettings.detailedLogsEnabled();
        System.setProperty("serenity.logging", detailed ? "VERBOSE" : "QUIET");

        Level rootLevel = detailed ? Level.INFO : Level.SEVERE;
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        if (rootLogger != null) {
            rootLogger.setLevel(rootLevel);
            for (Handler handler : rootLogger.getHandlers()) {
                handler.setLevel(rootLevel);
            }
        }

        Logger.getLogger("org.openqa.selenium").setLevel(detailed ? Level.WARNING : Level.SEVERE);
    }
}
