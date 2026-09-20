package pe.com.challenge.automation.libraries;

import pe.com.challenge.automation.managers.DataSourceManager;
import pe.com.challenge.automation.models.DataSourceDescriptor;
import pe.com.challenge.automation.utilities.ExcelUtility;

import java.util.Map;

/**
 * Punto reutilizable de acceso al libro de datos seleccionado por los tags del
 * escenario. El resto del framework no conoce rutas físicas ni Apache POI.
 */
public final class DataWorkbookLibrary {
    private DataWorkbookLibrary() {
    }

    public static Map<String, String> dataset(String id) {
        DataSourceDescriptor source = DataSourceManager.current();
        return ExcelUtility.getRowById(source.resourcePath(), source.sheetName(), id);
    }
}
