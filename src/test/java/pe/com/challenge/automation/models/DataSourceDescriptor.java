package pe.com.challenge.automation.models;

public record DataSourceDescriptor(
        String workbookName,
        String sheetName,
        String resourcePath) {
}
