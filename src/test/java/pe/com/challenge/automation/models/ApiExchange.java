package pe.com.challenge.automation.models;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ApiExchange(
        String method,
        String endpoint,
        Map<String, String> requestHeaders,
        Map<String, Object> requestParams,
        String requestBody,
        String headerErrorConfig,
        int responseStatus,
        String responseBody) {

    public ApiExchange {
        method = text(method);
        endpoint = text(endpoint);
        requestHeaders = requestHeaders == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(requestHeaders));
        requestParams = requestParams == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(requestParams));
        requestBody = text(requestBody);
        headerErrorConfig = text(headerErrorConfig);
        responseBody = text(responseBody);
    }

    public static ApiExchange empty() {
        return new ApiExchange("", "", Map.of(), Map.of(), "", "", 0, "");
    }

    public boolean available() {
        return !method.isBlank() || !endpoint.isBlank() || responseStatus > 0 || !responseBody.isBlank();
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
