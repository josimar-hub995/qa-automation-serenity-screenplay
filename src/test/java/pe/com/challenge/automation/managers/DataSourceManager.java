package pe.com.challenge.automation.managers;

import pe.com.challenge.automation.constants.ExcelConstants;
import pe.com.challenge.automation.exceptions.TestDataException;
import pe.com.challenge.automation.models.DataSourceDescriptor;
import pe.com.challenge.automation.utilities.ExcelUtility;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class DataSourceManager {
    private static final ThreadLocal<DataSourceDescriptor> CURRENT = new ThreadLocal<>();

    private DataSourceManager() {
    }

    public static void configure(Collection<String> scenarioTags) {
        List<String> workbookTags = scenarioTags.stream()
                .filter(DataSourceManager::isWorkbookTag)
                .toList();
        if (workbookTags.size() != 1) {
            throw new TestDataException(
                    "El escenario debe declarar exactamente un tag @xc-<Excel>; encontrados: " + workbookTags);
        }

        String workbookToken = workbookTags.get(0).substring(ExcelConstants.WORKBOOK_TAG_PREFIX.length());
        String workbookName = workbookToken.toLowerCase(Locale.ROOT)
                .endsWith(ExcelConstants.WORKBOOK_EXTENSION)
                ? workbookToken
                : workbookToken + ExcelConstants.WORKBOOK_EXTENSION;
        validateWorkbookName(workbookName);

        String resourcePath = ExcelConstants.DATA_DIRECTORY + workbookName;
        List<String> availableSheets = ExcelUtility.getSheetNames(resourcePath);
        ExcelUtility.validateUniqueColumnValues(resourcePath, ExcelConstants.TAP_COLUMN);
        List<String> selectedSheets = availableSheets.stream()
                .filter(sheet -> containsTag(scenarioTags, "@" + sheet))
                .toList();
        if (selectedSheets.size() != 1) {
            throw new TestDataException(
                    "El escenario debe declarar exactamente un tag de hoja. Hojas disponibles en "
                            + workbookName + ": " + availableSheets + "; coincidencias: " + selectedSheets);
        }

        CURRENT.set(new DataSourceDescriptor(workbookName, selectedSheets.get(0), resourcePath));
    }

    public static DataSourceDescriptor current() {
        DataSourceDescriptor source = CURRENT.get();
        if (source == null) {
            throw new IllegalStateException("No existe una fuente de datos configurada para el escenario");
        }
        return source;
    }

    public static void clear() {
        CURRENT.remove();
    }

    private static boolean isWorkbookTag(String tag) {
        return tag.regionMatches(true, 0,
                ExcelConstants.WORKBOOK_TAG_PREFIX, 0, ExcelConstants.WORKBOOK_TAG_PREFIX.length());
    }

    private static boolean containsTag(Collection<String> tags, String expectedTag) {
        return tags.stream().anyMatch(tag -> tag.equalsIgnoreCase(expectedTag));
    }

    private static void validateWorkbookName(String workbookName) {
        if (!workbookName.matches("[A-Za-z0-9][A-Za-z0-9._-]*\\.xlsx") || workbookName.contains("..")) {
            throw new TestDataException("Nombre de Excel inválido en el tag @xc-: " + workbookName);
        }
    }
}
