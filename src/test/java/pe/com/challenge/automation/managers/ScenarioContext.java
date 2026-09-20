package pe.com.challenge.automation.managers;

import java.util.HashMap;
import java.util.Map;

public final class ScenarioContext {
    private static final ThreadLocal<Map<String, Object>> VALUES = ThreadLocal.withInitial(HashMap::new);

    private ScenarioContext() {
    }

    public static void put(String key, Object value) {
        VALUES.get().put(key, value);
    }

    public static <T> T get(String key, Class<T> type) {
        Object value = VALUES.get().get(key);
        if (value == null) {
            throw new IllegalStateException("No existe el dato de contexto: " + key);
        }
        return type.cast(value);
    }

    public static void clear() {
        VALUES.remove();
    }
}
