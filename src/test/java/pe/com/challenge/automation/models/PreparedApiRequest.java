package pe.com.challenge.automation.models;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record PreparedApiRequest(
        String method,
        String endpoint,
        Map<String, String> headers,
        Map<String, Object> params,
        String body,
        String headerErrorConfig) {

    public PreparedApiRequest {
        headers = headers == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(headers));
        params = params == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(params));
        body = body == null ? "" : body;
        headerErrorConfig = headerErrorConfig == null ? "" : headerErrorConfig;
    }
}
