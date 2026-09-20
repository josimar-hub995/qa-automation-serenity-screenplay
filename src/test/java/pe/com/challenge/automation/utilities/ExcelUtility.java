package pe.com.challenge.automation.utilities;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pe.com.challenge.automation.constants.ExcelConstants;
import pe.com.challenge.automation.exceptions.TestDataException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ExcelUtility {
    private ExcelUtility() {
    }

    public static List<String> getSheetNames(String resourcePath) {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new TestDataException("No se encontró el Excel en classpath: " + resourcePath);
            }
            try (Workbook workbook = new XSSFWorkbook(input)) {
                List<String> sheetNames = new ArrayList<>();
                for (int index = 0; index < workbook.getNumberOfSheets(); index++) {
                    sheetNames.add(workbook.getSheetName(index));
                }
                return List.copyOf(sheetNames);
            }
        } catch (IOException e) {
            throw new TestDataException("No se pudo leer el Excel: " + resourcePath, e);
        }
    }

    public static Map<String, String> getRowById(String resourcePath, String sheetName, String id) {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new TestDataException("No se encontró el Excel en classpath: " + resourcePath);
            }
            try (Workbook workbook = new XSSFWorkbook(input)) {
                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    throw new TestDataException("No existe la hoja '" + sheetName + "' en " + resourcePath);
                }
                return findRow(sheet, id);
            }
        } catch (IOException e) {
            throw new TestDataException("No se pudo leer el Excel: " + resourcePath, e);
        }
    }

    public static void validateUniqueColumnValues(String resourcePath, String columnName) {
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new TestDataException("No se encontró el Excel en classpath: " + resourcePath);
            }
            try (Workbook workbook = new XSSFWorkbook(input)) {
                DataFormatter formatter = new DataFormatter();
                Map<String, String> firstLocation = new LinkedHashMap<>();
                for (Sheet sheet : workbook) {
                    Row header = sheet.getRow(sheet.getFirstRowNum());
                    int column = findColumn(header, columnName, formatter);
                    if (column < 0) {
                        continue;
                    }
                    for (int rowIndex = header.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                        Row row = sheet.getRow(rowIndex);
                        if (row == null) {
                            continue;
                        }
                        String value = formatter.formatCellValue(row.getCell(column)).trim();
                        if (value.isBlank()) {
                            continue;
                        }
                        String location = sheet.getSheetName() + "!" + (rowIndex + 1);
                        String previous = firstLocation.putIfAbsent(value.toUpperCase(), location);
                        if (previous != null) {
                            throw new TestDataException("El valor '" + value + "' de la columna " + columnName
                                    + " está duplicado en " + previous + " y " + location);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new TestDataException("No se pudo validar el Excel: " + resourcePath, e);
        }
    }

    private static Map<String, String> findRow(Sheet sheet, String id) {
        Row header = sheet.getRow(sheet.getFirstRowNum());
        if (header == null) {
            throw new TestDataException("La hoja '" + sheet.getSheetName() + "' no tiene encabezados");
        }

        DataFormatter formatter = new DataFormatter();
        int idColumn = -1;
        Map<Integer, String> headers = new LinkedHashMap<>();
        for (int column = 0; column < header.getLastCellNum(); column++) {
            String name = formatter.formatCellValue(header.getCell(column)).trim();
            if (!name.isBlank()) {
                headers.put(column, name);
            }
            if (ExcelConstants.ID_COLUMN.equalsIgnoreCase(name)) {
                idColumn = column;
            }
        }
        if (idColumn < 0) {
            throw new TestDataException("La hoja '" + sheet.getSheetName() + "' no contiene la columna "
                    + ExcelConstants.ID_COLUMN);
        }

        Map<String, String> result = null;
        for (int rowIndex = header.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }
            String currentId = formatter.formatCellValue(row.getCell(idColumn)).trim();
            if (id.equals(currentId)) {
                if (result != null) {
                    throw new TestDataException("El ID '" + id + "' está duplicado en la hoja '" + sheet.getSheetName() + "'");
                }
                result = new LinkedHashMap<>();
                for (Map.Entry<Integer, String> entry : headers.entrySet()) {
                    result.put(entry.getValue(), formatter.formatCellValue(row.getCell(entry.getKey())).trim());
                }
            }
        }
        if (result == null) {
            throw new TestDataException("No existe el ID '" + id + "' en la hoja '" + sheet.getSheetName() + "'");
        }
        return result;
    }

    private static int findColumn(Row header, String columnName, DataFormatter formatter) {
        if (header == null) {
            return -1;
        }
        for (int column = 0; column < header.getLastCellNum(); column++) {
            if (columnName.equalsIgnoreCase(formatter.formatCellValue(header.getCell(column)).trim())) {
                return column;
            }
        }
        return -1;
    }
}
