package pe.com.challenge.automation.reports;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import pe.com.challenge.automation.exceptions.FrameworkException;
import pe.com.challenge.automation.managers.ConfigurationManager;
import pe.com.challenge.automation.managers.ExecutionManager;
import pe.com.challenge.automation.models.ScenarioExecutionResult;
import pe.com.challenge.automation.models.StepExecutionResult;
import pe.com.challenge.automation.utilities.DateUtility;
import pe.com.challenge.automation.utilities.FileUtility;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class WordReportGenerator {
    private static final String NAVY = "0B2347";
    private static final String BLUE = "155EEF";
    private static final String PALE_BLUE = "EEF4FF";
    private static final String INK = "172033";
    private static final String MUTED = "667085";
    private static final String LINE = "DCE3EF";
    private static final String PASSED = "12A150";
    private static final String PASSED_SOFT = "EAF8F0";
    private static final String FAILED = "D92D3A";
    private static final String FAILED_SOFT = "FFF0F1";
    private static final int MAX_IMAGE_WIDTH_PX = 650;

    private WordReportGenerator() {
    }

    public static Path generate(ScenarioExecutionResult result, Path logo, Path target) {
        try (XWPFDocument document = new XWPFDocument()) {
            configurePage(document);
            addHeader(document, result, logo);
            addStatusStrip(document, result);
            addCaseInformation(document, result);
            addSectionTitle(document, "Ejecución Gherkin y evidencias", "01");
            addSteps(document, result);
            addApiEvidence(document, result);
            addFailureSummary(document, result);
            addClosing(document, result);

            FileUtility.createDirectories(target.getParent());
            try (OutputStream output = Files.newOutputStream(target)) {
                document.write(output);
            }
            return target;
        } catch (IOException e) {
            throw new FrameworkException("No se pudo generar el reporte Word individual: " + target, e);
        }
    }

    private static void configurePage(XWPFDocument document) {
        CTSectPr section = document.getDocument().getBody().isSetSectPr()
                ? document.getDocument().getBody().getSectPr()
                : document.getDocument().getBody().addNewSectPr();
        CTPageSz size = section.isSetPgSz() ? section.getPgSz() : section.addNewPgSz();
        size.setW(BigInteger.valueOf(11906));
        size.setH(BigInteger.valueOf(16838));
        CTPageMar margins = section.isSetPgMar() ? section.getPgMar() : section.addNewPgMar();
        margins.setTop(BigInteger.valueOf(650));
        margins.setBottom(BigInteger.valueOf(650));
        margins.setLeft(BigInteger.valueOf(720));
        margins.setRight(BigInteger.valueOf(720));
        margins.setHeader(BigInteger.valueOf(360));
        margins.setFooter(BigInteger.valueOf(360));
    }

    private static void addHeader(XWPFDocument document, ScenarioExecutionResult result, Path logo)
            throws IOException {
        XWPFTable header = document.createTable(1, 2);
        header.setWidth("100%");
        header.setCellMargins(0, 0, 0, 0);
        hideBorders(header);
        XWPFTableCell left = header.getRow(0).getCell(0);
        XWPFTableCell right = header.getRow(0).getCell(1);
        setCellWidth(left, 78);
        setCellWidth(right, 22);

        XWPFParagraph eyebrow = firstParagraph(left);
        eyebrow.setSpacingAfter(35);
        addRun(eyebrow, "AUTOMATION EVIDENCE REPORT", 8, true, BLUE);

        XWPFParagraph title = left.addParagraph();
        title.setSpacingAfter(45);
        addRun(title, result.testCaseId(), 21, true, NAVY);

        XWPFParagraph subtitle = left.addParagraph();
        subtitle.setSpacingAfter(0);
        addRun(subtitle, result.scenarioName(), 11, false, INK);

        XWPFParagraph logoParagraph = firstParagraph(right);
        logoParagraph.setAlignment(ParagraphAlignment.RIGHT);
        logoParagraph.setSpacingAfter(0);
        addImageToParagraph(logoParagraph, logo, 88);
        XWPFParagraph product = right.addParagraph();
        product.setAlignment(ParagraphAlignment.RIGHT);
        product.setSpacingBefore(25);
        addRun(product, isApi(result) ? "REQRES API" : "SELENIUM WEB", 8, true, MUTED);

        XWPFParagraph divider = document.createParagraph();
        divider.setBorderBottom(org.apache.poi.xwpf.usermodel.Borders.SINGLE);
        divider.setSpacingAfter(80);
    }

    private static void addStatusStrip(XWPFDocument document, ScenarioExecutionResult result) {
        XWPFTable strip = document.createTable(1, 3);
        strip.setWidth("100%");
        strip.setCellMargins(75, 110, 75, 110);
        hideBorders(strip);
        boolean passed = isPassed(result.status());
        styleStripCell(strip.getRow(0).getCell(0), "ESTADO", passed ? "PASSED" : "FAILED",
                passed ? PASSED_SOFT : FAILED_SOFT, passed ? PASSED : FAILED);
        styleStripCell(strip.getRow(0).getCell(1), "FECHA Y HORA",
                DateUtility.stepDisplay(result.startTime()), PALE_BLUE, NAVY);
        styleStripCell(strip.getRow(0).getCell(2), "DURACIÓN",
                formatDuration(result.duration()), PALE_BLUE, NAVY);
        spacer(document, 90);
    }

    private static void styleStripCell(XWPFTableCell cell, String label, String value,
                                       String background, String valueColor) {
        shade(cell, background);
        XWPFParagraph paragraph = firstParagraph(cell);
        paragraph.setSpacingAfter(0);
        addRun(paragraph, label + "  ", 7, true, MUTED);
        addRun(paragraph, value, 10, true, valueColor);
    }

    private static void addCaseInformation(XWPFDocument document, ScenarioExecutionResult result) {
        addSectionTitle(document, "Datos del caso de prueba", "00");
        XWPFTable table = document.createTable(11, 2);
        table.setWidth("100%");
        table.setCellMargins(75, 100, 75, 100);
        applyGridBorders(table, LINE);
        setDataRow(table, 0, "TAP", result.testCaseId());
        setDataRow(table, 1, "Escenario", result.scenarioName());
        setDataRow(table, 2, "Resultado esperado", result.expectedResult());
        setDataRow(table, 3, "Canal / navegador", result.browser());
        setDataRow(table, 4, "Página / servicio", result.targetUrl());
        setDataRow(table, 5, "Excel / hoja", result.workbookName() + " / " + result.sheetName());
        setDataRow(table, 6, "Datos", result.datasetId());
        setDataRow(table, 7, "Ambiente", ConfigurationManager.environment());
        setDataRow(table, 8, "Inicio", DateUtility.stepDisplay(result.startTime()));
        setDataRow(table, 9, "Fin", DateUtility.stepDisplay(result.endTime()));
        setDataRow(table, 10, "Ejecución", ExecutionManager.executionPath().getFileName().toString());
        spacer(document, 120);
    }

    private static void setDataRow(XWPFTable table, int index, String key, String value) {
        XWPFTableCell keyCell = table.getRow(index).getCell(0);
        XWPFTableCell valueCell = table.getRow(index).getCell(1);
        setCellWidth(keyCell, 27);
        setCellWidth(valueCell, 73);
        shade(keyCell, index % 2 == 0 ? NAVY : "12335F");
        shade(valueCell, index % 2 == 0 ? "FFFFFF" : "F7F9FC");
        setCellText(keyCell, key, 8, true, "FFFFFF", ParagraphAlignment.LEFT);
        setCellText(valueCell, value, 9, false, INK, ParagraphAlignment.LEFT);
    }

    private static void addSteps(XWPFDocument document, ScenarioExecutionResult result) throws IOException {
        Set<Path> inserted = new HashSet<>();
        if (result.steps().isEmpty()) {
            addNotice(document,
                    "No se registraron pasos Gherkin. El escenario falló durante la preparación o configuración.",
                    FAILED_SOFT, FAILED);
        }
        for (StepExecutionResult step : result.steps()) {
            if (step.sequence() > 1 && !step.screenshots().isEmpty()) {
                document.createParagraph().createRun().addBreak(BreakType.PAGE);
            }
            boolean passed = isPassed(step.status());
            XWPFTable heading = document.createTable(1, 2);
            heading.setWidth("100%");
            heading.setCellMargins(90, 110, 90, 110);
            hideBorders(heading);
            XWPFTableCell stepCell = heading.getRow(0).getCell(0);
            XWPFTableCell stateCell = heading.getRow(0).getCell(1);
            setCellWidth(stepCell, 82);
            setCellWidth(stateCell, 18);
            shade(stepCell, "F7F9FC");
            shade(stateCell, passed ? PASSED_SOFT : FAILED_SOFT);
            setCellText(stepCell,
                    DateUtility.stepDisplay(step.startTime()) + " | " + step.gherkinText(),
                    10, true, NAVY, ParagraphAlignment.LEFT);
            setCellText(stateCell, passed ? "PASSED" : "FAILED",
                    9, true, passed ? PASSED : FAILED, ParagraphAlignment.CENTER);

            XWPFParagraph detail = document.createParagraph();
            detail.setIndentationLeft(100);
            detail.setSpacingBefore(45);
            detail.setSpacingAfter(70);
            addRun(detail, "Paso %02d · duración %s".formatted(
                    step.sequence(), formatDuration(step.duration())), 8, false, MUTED);

            if (!passed) {
                addNotice(document,
                        "La automatización se detuvo en este paso. Causa: "
                                + defaultText(step.errorMessage(), result.errorMessage()),
                        FAILED_SOFT, FAILED);
            }

            for (Path screenshot : step.screenshots()) {
                if (Files.exists(screenshot)) {
                    addScreenshot(document, screenshot,
                            "%s · Evidencia del paso %02d".formatted(
                                    result.testCaseId(), step.sequence()));
                    inserted.add(screenshot.toAbsolutePath().normalize());
                }
            }
            if (step.screenshots().isEmpty() && isApi(result)) {
                XWPFParagraph apiNote = document.createParagraph();
                apiNote.setIndentationLeft(100);
                apiNote.setSpacingAfter(100);
                addRun(apiNote, "Paso API: la evidencia técnica se conserva en api_exchange.json, api_response.json y Serenity.",
                        8, false, MUTED);
            }
            spacer(document, 90);
        }

        if (!isPassed(result.status()) && !result.screenshots().isEmpty()) {
            Path last = result.screenshots().get(result.screenshots().size() - 1);
            if (Files.exists(last) && !inserted.contains(last.toAbsolutePath().normalize())) {
                addSubheading(document, "Último estado capturado antes del fallo", FAILED);
                addScreenshot(document, last,
                        result.testCaseId() + " · Evidencia final del fallo");
            }
        }
    }

    private static void addApiEvidence(XWPFDocument document, ScenarioExecutionResult result) throws IOException {
        Path response = result.scenarioPath().resolve("result/api_response.json");
        if (!Files.exists(response)) {
            return;
        }
        addSubheading(document, "Respuesta API registrada", BLUE);
        String payload = Files.readString(response, StandardCharsets.UTF_8);
        if (payload.length() > 6000) {
            payload = payload.substring(0, 6000) + System.lineSeparator() + "… contenido truncado …";
        }
        XWPFTable code = document.createTable(1, 1);
        code.setWidth("100%");
        hideBorders(code);
        XWPFTableCell cell = code.getRow(0).getCell(0);
        shade(cell, "101828");
        XWPFParagraph paragraph = firstParagraph(cell);
        paragraph.setSpacingBefore(80);
        paragraph.setSpacingAfter(80);
        XWPFRun run = addRun(paragraph, payload, 8, false, "D0D5DD");
        run.setFontFamily("Consolas");
        spacer(document, 100);
    }

    private static void addFailureSummary(XWPFDocument document, ScenarioExecutionResult result) {
        if (isPassed(result.status())) {
            return;
        }
        addSectionTitle(document, "Análisis del fallo", "02");
        XWPFTable box = document.createTable(3, 2);
        box.setWidth("100%");
        box.setCellMargins(85, 100, 85, 100);
        applyGridBorders(box, "FFC8CC");
        setFailureRow(box, 0, "Paso o contexto", defaultText(result.failedStep(), "Ejecución del escenario"));
        setFailureRow(box, 1, "Error", defaultText(result.errorMessage(), "Sin mensaje técnico disponible"));
        String trace = defaultText(result.stackTrace(), "Stack trace no disponible");
        if (trace.length() > 5000) {
            trace = trace.substring(0, 5000) + System.lineSeparator() + "… stack trace truncado …";
        }
        setFailureRow(box, 2, "Stack trace", trace);
    }

    private static void setFailureRow(XWPFTable table, int row, String key, String value) {
        XWPFTableCell keyCell = table.getRow(row).getCell(0);
        XWPFTableCell valueCell = table.getRow(row).getCell(1);
        setCellWidth(keyCell, 24);
        setCellWidth(valueCell, 76);
        shade(keyCell, FAILED);
        shade(valueCell, FAILED_SOFT);
        setCellText(keyCell, key, 8, true, "FFFFFF", ParagraphAlignment.LEFT);
        XWPFParagraph paragraph = firstParagraph(valueCell);
        paragraph.setSpacingAfter(0);
        XWPFRun run = addRun(paragraph, value, row == 2 ? 7 : 8, false, row == 2 ? "5C2930" : INK);
        if (row == 2) {
            run.setFontFamily("Consolas");
        }
    }

    private static void addClosing(XWPFDocument document, ScenarioExecutionResult result) {
        spacer(document, 130);
        XWPFParagraph summary = document.createParagraph();
        summary.setAlignment(ParagraphAlignment.CENTER);
        summary.setSpacingBefore(80);
        summary.setSpacingAfter(40);
        boolean passed = isPassed(result.status());
        addRun(summary,
                passed ? "CASO FINALIZADO · PASSED" : "CASO FINALIZADO · FAILED",
                11, true, passed ? PASSED : FAILED);
        XWPFParagraph note = document.createParagraph();
        note.setAlignment(ParagraphAlignment.CENTER);
        addRun(note,
                "Documento individual generado automáticamente · "
                        + ExecutionManager.executionPath().getFileName(),
                7, false, MUTED);
    }

    private static void addSectionTitle(XWPFDocument document, String title, String index) {
        XWPFTable heading = document.createTable(1, 2);
        heading.setWidth("100%");
        heading.setCellMargins(50, 0, 50, 0);
        hideBorders(heading);
        setCellWidth(heading.getRow(0).getCell(0), 8);
        setCellWidth(heading.getRow(0).getCell(1), 92);
        XWPFTableCell indexCell = heading.getRow(0).getCell(0);
        XWPFTableCell titleCell = heading.getRow(0).getCell(1);
        shade(indexCell, NAVY);
        setCellText(indexCell, index, 8, true, "FFFFFF", ParagraphAlignment.CENTER);
        setCellText(titleCell, title, 13, true, NAVY, ParagraphAlignment.LEFT);
        spacer(document, 45);
    }

    private static void addSubheading(XWPFDocument document, String text, String color) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBefore(120);
        paragraph.setSpacingAfter(70);
        addRun(paragraph, text, 11, true, color);
    }

    private static void addNotice(XWPFDocument document, String text, String background, String color) {
        XWPFTable table = document.createTable(1, 1);
        table.setWidth("100%");
        table.setCellMargins(90, 110, 90, 110);
        hideBorders(table);
        XWPFTableCell cell = table.getRow(0).getCell(0);
        shade(cell, background);
        setCellText(cell, text, 8, true, color, ParagraphAlignment.LEFT);
        spacer(document, 70);
    }

    private static void addScreenshot(XWPFDocument document, Path image, String caption) throws IOException {
        BufferedImage buffered = ImageIO.read(image.toFile());
        if (buffered == null) {
            return;
        }
        int width = Math.min(MAX_IMAGE_WIDTH_PX, buffered.getWidth());
        int height = Math.max(1, (int) Math.round(width * buffered.getHeight()
                / (double) buffered.getWidth()));
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setSpacingBefore(40);
        paragraph.setSpacingAfter(35);
        try (InputStream input = Files.newInputStream(image)) {
            paragraph.createRun().addPicture(
                    input,
                    XWPFDocument.PICTURE_TYPE_PNG,
                    image.getFileName().toString(),
                    Units.pixelToEMU(width),
                    Units.pixelToEMU(height));
        } catch (InvalidFormatException e) {
            throw new IOException("Formato de imagen no válido: " + image, e);
        }
        XWPFParagraph legend = document.createParagraph();
        legend.setAlignment(ParagraphAlignment.CENTER);
        legend.setSpacingAfter(100);
        addRun(legend, caption + " · " + image.getFileName(), 7, false, MUTED);
    }

    private static void addImageToParagraph(XWPFParagraph paragraph, Path image, int widthPixels)
            throws IOException {
        BufferedImage buffered = ImageIO.read(image.toFile());
        if (buffered == null) {
            return;
        }
        int heightPixels = Math.max(1, (int) Math.round(widthPixels * buffered.getHeight()
                / (double) buffered.getWidth()));
        try (InputStream input = Files.newInputStream(image)) {
            paragraph.createRun().addPicture(input, XWPFDocument.PICTURE_TYPE_PNG,
                    image.getFileName().toString(),
                    Units.pixelToEMU(widthPixels), Units.pixelToEMU(heightPixels));
        } catch (InvalidFormatException e) {
            throw new IOException("Formato de logo no válido: " + image, e);
        }
    }

    private static XWPFParagraph firstParagraph(XWPFTableCell cell) {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        paragraph.setSpacingBefore(0);
        paragraph.setSpacingAfter(0);
        return paragraph;
    }

    private static void setCellText(XWPFTableCell cell, String text, int size, boolean bold,
                                    String color, ParagraphAlignment alignment) {
        XWPFParagraph paragraph = firstParagraph(cell);
        paragraph.setAlignment(alignment);
        addRun(paragraph, text == null ? "" : text, size, bold, color);
    }

    private static XWPFRun addRun(XWPFParagraph paragraph, String text, int size,
                                  boolean bold, String color) {
        XWPFRun run = paragraph.createRun();
        run.setText(text == null ? "" : text);
        run.setFontFamily("Arial");
        run.setFontSize(size);
        run.setBold(bold);
        run.setColor(color);
        return run;
    }

    private static void shade(XWPFTableCell cell, String color) {
        cell.setColor(color);
    }

    private static void setCellWidth(XWPFTableCell cell, int percent) {
        cell.setWidth(percent + "%");
    }

    private static void hideBorders(XWPFTable table) {
        table.removeBorders();
    }

    private static void applyGridBorders(XWPFTable table, String color) {
        XWPFTable.XWPFBorderType type = XWPFTable.XWPFBorderType.SINGLE;
        table.setTopBorder(type, 4, 0, color);
        table.setBottomBorder(type, 4, 0, color);
        table.setLeftBorder(type, 4, 0, color);
        table.setRightBorder(type, 4, 0, color);
        table.setInsideHBorder(type, 4, 0, color);
        table.setInsideVBorder(type, 4, 0, color);
    }

    private static void spacer(XWPFDocument document, int after) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(after);
    }

    private static boolean isPassed(String status) {
        return "PASSED".equalsIgnoreCase(status);
    }

    private static boolean isApi(ScenarioExecutionResult result) {
        return result.executionTag().startsWith("TC_API_");
    }

    private static String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String formatDuration(Duration duration) {
        return String.format(Locale.US, "%.2f s", duration.toMillis() / 1000.0);
    }
}
