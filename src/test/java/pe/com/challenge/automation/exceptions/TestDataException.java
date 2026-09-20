package pe.com.challenge.automation.exceptions;

public class TestDataException extends RuntimeException {
    public TestDataException(String message) {
        super(message);
    }

    public TestDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
