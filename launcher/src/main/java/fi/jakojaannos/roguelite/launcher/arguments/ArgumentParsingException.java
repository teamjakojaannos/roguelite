package fi.jakojaannos.roguelite.launcher.arguments;

public class ArgumentParsingException extends Exception {
    public ArgumentParsingException(String message) {
        super(message);
    }

    public ArgumentParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
