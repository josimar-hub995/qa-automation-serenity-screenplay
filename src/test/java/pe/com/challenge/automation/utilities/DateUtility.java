package pe.com.challenge.automation.utilities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class DateUtility {
    private static final DateTimeFormatter FOLDER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern(
            "dd-MM-yyyy | hh:mm:ss a", Locale.US);
    private static final DateTimeFormatter STEP_FORMAT = DateTimeFormatter.ofPattern(
            "dd-MM-yyyy | hh:mm:ss a", Locale.US);

    private DateUtility() {
    }

    public static String folderTimestamp() {
        return LocalDateTime.now().format(FOLDER_FORMAT);
    }

    public static String display(LocalDateTime dateTime) {
        return dateTime.format(DISPLAY_FORMAT).toUpperCase(Locale.ROOT);
    }

    public static String stepDisplay(LocalDateTime dateTime) {
        return dateTime.format(STEP_FORMAT).toUpperCase(Locale.ROOT);
    }
}
