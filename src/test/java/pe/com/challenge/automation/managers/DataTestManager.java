package pe.com.challenge.automation.managers;

import pe.com.challenge.automation.constants.ExcelConstants;
import pe.com.challenge.automation.exceptions.TestDataException;
import pe.com.challenge.automation.libraries.DataWorkbookLibrary;
import pe.com.challenge.automation.models.ApiTestData;
import pe.com.challenge.automation.models.DocumentationTestData;
import pe.com.challenge.automation.models.HomeTestData;
import pe.com.challenge.automation.models.SearchTestData;
import pe.com.challenge.automation.utilities.StringUtility;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class DataTestManager {

    private DataTestManager() {
    }

    public static HomeTestData getHomeData(String id) {
        Map<String, String> row = row(id);

        return new HomeTestData(
                id,
                requiredTap(row),
                required(row, "ESCENARIO"),
                required(row, "RESULTADO_ESPERADO"),
                StringUtility.isTrue(required(row, "EJECUTAR"))
        );
    }

    public static DocumentationTestData getDocumentationData(String id) {
        Map<String, String> row = row(id);

        return new DocumentationTestData(
                id,
                requiredTap(row),
                required(row, "ESCENARIO"),
                required(row, "RESULTADO_ESPERADO"),
                StringUtility.isTrue(required(row, "EJECUTAR"))
        );
    }

    public static SearchTestData getSearchData(String id) {
        Map<String, String> row = row(id);

        return new SearchTestData(
                id,
                requiredTap(row),
                required(row, "ESCENARIO"),
                required(row, "RESULTADO_ESPERADO"),
                required(row, "VALOR_BUSQUEDA"),
                StringUtility.isTrue(required(row, "EJECUTAR"))
        );
    }

    public static ApiTestData getApiData(String id) {
        Map<String, String> row = row(id);

        ApiTestData data = new ApiTestData(
                id,
                requiredTap(row),
                required(row, "ESCENARIO"),
                required(row, "RESULTADO_ESPERADO"),
                requiredInt(row, "ITERACION"),
                required(row, "CAPA"),
                required(row, "VERSION"),
                required(row, "API"),
                required(row, "CREDENCIALES"),
                required(row, "ENDPOINT"),
                required(row, "HEADERS"),
                optional(row, "HEADER_ERROR_CONFIG"),
                required(row, "PARAMS"),
                required(row, "REQUEST_TYPE"),
                required(row, "BODY"),
                requiredInt(row, "ESTADO_HTTP_ESPERADO"),
                required(row, "RESPUESTA_ESPERADA"),
                StringUtility.isTrue(required(row, "EJECUTAR"))
        );

        validateApiData(data);

        return data;
    }

    private static Map<String, String> row(String id) {
        return DataWorkbookLibrary.dataset(id);
    }

    private static String required(Map<String, String> row, String column) {
        String value = row.get(column);

        if (value == null || value.isBlank()) {
            throw new TestDataException(
                    "La columna obligatoria '" + column + "' no tiene valor"
            );
        }

        return value.trim();
    }

    private static int requiredInt(Map<String, String> row, String column) {
        String value = required(row, column);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException error) {
            throw new TestDataException(
                    "La columna '" + column + "' debe contener un número entero",
                    error
            );
        }
    }

    private static String optional(Map<String, String> row, String column) {
        String value = row.get(column);

        return value == null
                ? ""
                : value.trim();
    }

    private static String requiredTap(Map<String, String> row) {
        String tap = required(row, ExcelConstants.TAP_COLUMN)
                .replace('\u00A0', ' ')
                .trim();

        if (!tap.matches("(?i)^TAP\\s*-\\s*\\d{3,}$")) {
            throw new TestDataException(
                    "La columna TAP debe contener un identificador como TAP - 001"
            );
        }

        String number = tap.replaceFirst(
                "(?i)^TAP\\s*-\\s*",
                ""
        );

        return "TAP - " + number;
    }

    private static void validateApiData(ApiTestData data) {
        if (!"API".equalsIgnoreCase(data.layer())) {
            throw new TestDataException(
                    "La columna CAPA debe contener API para los casos de servicio"
            );
        }

        String requestType = data.requestType()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!Set.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE"
        ).contains(requestType)) {

            throw new TestDataException(
                    "REQUEST_TYPE no soportado: "
                            + data.requestType()
                            + ". Use GET, POST, PUT, PATCH o DELETE"
            );
        }

        try {
            URI endpoint = new URI(data.endpoint());

            if (!endpoint.isAbsolute()
                    || !(
                    "http".equalsIgnoreCase(endpoint.getScheme())
                            || "https".equalsIgnoreCase(endpoint.getScheme())
            )) {

                throw new TestDataException(
                        "ENDPOINT debe contener una URL HTTP(S) absoluta: "
                                + data.endpoint()
                );
            }

        } catch (URISyntaxException error) {

            throw new TestDataException(
                    "ENDPOINT no contiene una URL válida: "
                            + data.endpoint(),
                    error
            );
        }
    }
}