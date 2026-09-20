package pe.com.challenge.automation.builders;

import pe.com.challenge.automation.models.ApiTestData;
import pe.com.challenge.automation.models.PreparedApiRequest;
import pe.com.challenge.automation.utilities.JsonUtility;

import java.util.Locale;

public final class ApiRequestBuilder {
    private ApiRequestBuilder() {
    }

    public static PreparedApiRequest from(ApiTestData data) {
        return new PreparedApiRequest(
                data.requestType().trim().toUpperCase(Locale.ROOT),
                data.endpoint().trim(),
                ApiHeaderBuilder.build(data.headers(), data.headerErrorConfig()),
                JsonUtility.readObject(data.params(), "PARAMS"),
                normalizeJson(data.body(), "BODY"),
                data.headerErrorConfig());
    }

    private static String normalizeJson(String value, String column) {
        if (value == null || value.isBlank()) {
            return "";
        }
        JsonUtility.readTree(value, column);
        return value.trim();
    }
}
