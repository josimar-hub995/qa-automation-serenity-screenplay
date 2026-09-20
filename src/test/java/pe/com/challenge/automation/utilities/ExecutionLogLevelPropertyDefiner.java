package pe.com.challenge.automation.utilities;

import ch.qos.logback.core.PropertyDefinerBase;

/** Permite que Logback use la bandera execution.logs de serenity.properties. */
public final class ExecutionLogLevelPropertyDefiner extends PropertyDefinerBase {
    @Override
    public String getPropertyValue() {
        return ExecutionSettings.detailedLogsEnabled() ? "INFO" : "OFF";
    }
}
