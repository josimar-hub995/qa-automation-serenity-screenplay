package pe.com.challenge.automation.utilities;

import pe.com.challenge.automation.managers.ConfigurationManager;

import java.time.Duration;

public final class WaitUtility {
    private WaitUtility() {
    }

    public static Duration explicitTimeout() {
        return Duration.ofSeconds(Long.parseLong(ConfigurationManager.get("explicit.wait.seconds")));
    }
}
