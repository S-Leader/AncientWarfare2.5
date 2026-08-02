package net.minecraft.command;

/** Compatibility exception for malformed or out-of-range numeric arguments. */
public class NumberInvalidException extends CommandException {
    public NumberInvalidException(String message) {
        super(message);
    }

    public NumberInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
