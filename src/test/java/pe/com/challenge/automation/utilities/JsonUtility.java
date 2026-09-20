package pe.com.challenge.automation.utilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import pe.com.challenge.automation.exceptions.FrameworkException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class JsonUtility {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    private JsonUtility() {
    }

    public static void write(Path path, Object value) {
        try {
            FileUtility.createDirectories(path.getParent());
            MAPPER.writeValue(path.toFile(), value);
        } catch (IOException e) {
            throw new FrameworkException("No se pudo escribir el JSON: " + path, e);
        }
    }

    public static Map<String, Object> readObject(String json, String sourceName) {
        JsonNode node = readTree(json, sourceName);
        if (!node.isObject()) {
            throw new FrameworkException(sourceName + " debe contener un objeto JSON");
        }
        try {
            return MAPPER.convertValue(node,
                    MAPPER.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class));
        } catch (IllegalArgumentException error) {
            throw new FrameworkException("No se pudo convertir " + sourceName + " a un objeto JSON", error);
        }
    }

    public static JsonNode readTree(String json, String sourceName) {
        String value = json == null || json.isBlank() ? "{}" : json.trim();
        try {
            return MAPPER.readTree(value);
        } catch (IOException error) {
            throw new FrameworkException(sourceName + " no contiene JSON válido: " + error.getMessage(), error);
        }
    }

    public static String pretty(String json) {
        if (json == null || json.isBlank()) {
            return "";
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(MAPPER.readTree(json));
        } catch (IOException error) {
            return json;
        }
    }
}
