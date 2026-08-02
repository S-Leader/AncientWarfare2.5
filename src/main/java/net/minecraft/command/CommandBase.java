package net.minecraft.command;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Minimal source-compatible subset of the 1.12 CommandBase API. */
public abstract class CommandBase {
    public int getRequiredPermissionLevel() {
        return 4;
    }

    public abstract String getName();

    public abstract String getUsage(ICommandSender sender);

    public abstract void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException;

    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(getRequiredPermissionLevel(), getName());
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                           @Nullable BlockPos targetPos) {
        return Collections.emptyList();
    }

    public boolean isUsernameIndex(String[] args, int index) {
        return false;
    }

    public static int parseInt(String value) throws NumberInvalidException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new NumberInvalidException("Invalid integer: " + value, ex);
        }
    }

    public static int parseInt(String value, int minimum, int maximum) throws NumberInvalidException {
        int parsed = parseInt(value);
        if (parsed < minimum || parsed > maximum) {
            throw new NumberInvalidException("Integer out of range [" + minimum + ", " + maximum + "]: " + value);
        }
        return parsed;
    }

    public static double parseDouble(double base, String value, double minimum, double maximum,
                                     boolean centerBlock) throws NumberInvalidException {
        boolean relative = value.startsWith("~");
        String numeric = relative ? value.substring(1) : value;
        double parsed = relative ? base : 0.0D;
        if (!numeric.isEmpty()) {
            try {
                parsed += Double.parseDouble(numeric);
            } catch (NumberFormatException ex) {
                throw new NumberInvalidException("Invalid number: " + value, ex);
            }
        }
        if (!relative && centerBlock && numeric.indexOf('.') < 0) {
            parsed += 0.5D;
        }
        if (parsed < minimum || parsed > maximum) {
            throw new NumberInvalidException("Number out of range [" + minimum + ", " + maximum + "]: " + value);
        }
        return parsed;
    }

    public static List<String> getListOfStringsMatchingLastWord(String[] args, String... candidates) {
        return getListOfStringsMatchingLastWord(args, List.of(candidates));
    }

    public static List<String> getListOfStringsMatchingLastWord(String[] args, Collection<String> candidates) {
        String prefix = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String candidate : candidates) {
            if (candidate != null && candidate.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                result.add(candidate);
            }
        }
        return result;
    }
}
