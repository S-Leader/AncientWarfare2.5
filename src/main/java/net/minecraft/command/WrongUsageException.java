package net.minecraft.command;

/** Compatibility exception for command usage errors. */
public class WrongUsageException extends CommandException {
    public WrongUsageException(String message) {
        super(message);
    }
}
