package pe.com.challenge.automation.reports;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pe.com.challenge.automation.exceptions.FrameworkException;
import pe.com.challenge.automation.models.ScenarioExecutionResult;
import pe.com.challenge.automation.utilities.DateUtility;
import pe.com.challenge.automation.utilities.FileUtility;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ExecutionExcelReportGenerator {
    private ExecutionExcelReportGenerator() {
    }

    public static Path generate(List<ScenarioExecutionResult> results, Path target) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Resultados");
            sheet.createFreezePane(0, 5);
            sheet.setDisplayGridlines(false);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFont(titleFont);

            Row titleRow = sheet.createRow(0);
            Cell title = titleRow.createCell(0);
            title.setCellValue(reportTitle(results));
            title.setCellStyle(titleStyle);

            long passed = results.stream().filter(result -> "PASSED".equalsIgnoreCase(result.status())).count();
            long failed = results.size() - passed;
            Row metrics = sheet.createRow(2);
            metrics.createCell(0).setCellValue("Total");
            metrics.createCell(1).setCellValue(results.size());
            metrics.createCell(2).setCellValue("PASSED");
            metrics.createCell(3).setCellValue(passed);
            metrics.createCell(4).setCellValue("FAILED");
            metrics.createCell(5).setCellValue(failed);
            metrics.createCell(6).setCellValue("Pass rate");
            metrics.createCell(7).setCellValue(results.isEmpty() ? 0 : passed / (double) results.size());
            CellStyle percentageStyle = workbook.createCellStyle();
            percentageStyle.setDataFormat(workbook.createDataFormat().getFormat("0%"));
            metrics.getCell(7).setCellStyle(percentageStyle);

            String[] headers = {
                    "TAP", "Escenario", "Resultado esperado", "Datos",
                    "Excel", "Hoja", "Estado", "Inicio", "Fin", "Duración (s)", "Evidencias",
                    "Canal / navegador", "Página / servicio", "Paso fallido", "Error", "Ruta"
            };
            Row header = sheet.createRow(4);
            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int column = 0; column < headers.length; column++) {
                Cell cell = header.createCell(column);
                cell.setCellValue(headers[column]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 5;
            CellStyle narrativeStyle = workbook.createCellStyle();
            narrativeStyle.setWrapText(true);
            narrativeStyle.setVerticalAlignment(VerticalAlignment.TOP);
            for (ScenarioExecutionResult result : results) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(result.testCaseId());
                row.createCell(1).setCellValue(result.scenarioName());
                row.getCell(1).setCellStyle(narrativeStyle);
                row.createCell(2).setCellValue(result.expectedResult());
                row.getCell(2).setCellStyle(narrativeStyle);
                row.createCell(3).setCellValue(result.datasetId());
                row.createCell(4).setCellValue(result.workbookName());
                row.createCell(5).setCellValue(result.sheetName());
                Cell status = row.createCell(6);
                status.setCellValue(normalizedStatus(result.status()));
                status.setCellStyle(createStatusStyle(workbook, result.status()));
                row.createCell(7).setCellValue(DateUtility.display(result.startTime()));
                row.createCell(8).setCellValue(DateUtility.display(result.endTime()));
                row.createCell(9).setCellValue(result.duration().toMillis() / 1000.0);
                row.createCell(10).setCellValue(result.screenshots().size());
                row.createCell(11).setCellValue(result.browser());
                row.createCell(12).setCellValue(result.targetUrl());
                row.createCell(13).setCellValue(result.failedStep());
                row.createCell(14).setCellValue(result.errorMessage());
                row.getCell(14).setCellStyle(narrativeStyle);
                row.createCell(15).setCellValue(result.scenarioPath().toString());
                row.setHeightInPoints(48);
            }

            int[] widths = {18, 65, 58, 12, 22, 22, 14, 28, 28, 15, 12,
                    20, 52, 45, 65, 55};
            for (int column = 0; column < widths.length; column++) {
                sheet.setColumnWidth(column, widths[column] * 256);
            }
            FileUtility.createDirectories(target.getParent());
            try (OutputStream output = Files.newOutputStream(target)) {
                workbook.write(output);
            }
            return target;
        } catch (IOException e) {
            throw new FrameworkException("No se pudo generar el Excel de resultados: " + target, e);
        }
    }

    private static CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        return style;
    }

    private static CellStyle createStatusStyle(XSSFWorkbook workbook, String status) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        short color = switch (status.toUpperCase()) {
            case "PASSED" -> IndexedColors.GREEN.getIndex();
            case "FAILED" -> IndexedColors.RED.getIndex();
            default -> IndexedColors.RED.getIndex();
        };
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static String normalizedStatus(String status) {
        return "PASSED".equalsIgnoreCase(status) ? "PASSED" : "FAILED";
    }

    private static String reportTitle(List<ScenarioExecutionResult> results) {
        boolean hasApi = results.stream().anyMatch(result -> result.executionTag().startsWith("TC_API_"));
        boolean hasWeb = results.stream().anyMatch(result -> !result.executionTag().startsWith("TC_API_"));
        if (hasWeb && hasApi) {
            return "Resultado de ejecución Web y API";
        }
        if (hasApi) {
            return "Resultado de ejecución API";
        }
        if (hasWeb) {
            return "Resultado de ejecución Web";
        }
        return "Resultado de ejecución";
    }
}
