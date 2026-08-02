package net.shadowmage.ancientwarfare.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Bridges the original AW2 command tree onto the 1.20.1 Brigadier dispatcher.
 */
public final class LegacyCommandRegistrar {
    private LegacyCommandRegistrar() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBase command) {
        dispatcher.register(Commands.literal(command.getName())
                .requires(source -> command.checkPermission(source.getServer(), new SenderAdapter(source)))
                .executes(context -> execute(command, context, new String[0]))
                .then(Commands.argument("args", StringArgumentType.greedyString())
                        .suggests((context, builder) -> suggest(command, context, builder))
                        .executes(context -> execute(command, context,
                                splitArguments(StringArgumentType.getString(context, "args"))))));
    }

    private static int execute(CommandBase command, CommandContext<CommandSourceStack> context, String[] args) {
        SenderAdapter sender = new SenderAdapter(context.getSource());
        try {
            command.execute(context.getSource().getServer(), sender, args);
            return 1;
        } catch (CommandException | RuntimeException ex) {
            context.getSource().sendFailure(Component.literal(ex.getMessage() == null ? command.getUsage(sender) : ex.getMessage()));
            return 0;
        }
    }

    private static CompletableFuture<Suggestions> suggest(CommandBase command,
                                                          CommandContext<CommandSourceStack> context,
                                                          SuggestionsBuilder builder) {
        String raw = builder.getInput().substring(Math.min(builder.getStart(), builder.getInput().length()));
        String[] args = splitArguments(raw);
        if (raw.endsWith(" ")) {
            String[] extended = new String[args.length + 1];
            System.arraycopy(args, 0, extended, 0, args.length);
            extended[args.length] = "";
            args = extended;
        }
        List<String> suggestions = command.getTabCompletions(context.getSource().getServer(),
                new SenderAdapter(context.getSource()), args, BlockPos.containing(context.getSource().getPosition()));
        for (String suggestion : suggestions) {
            builder.suggest(suggestion);
        }
        return builder.buildFuture();
    }

    private static String[] splitArguments(String raw) {
        if (raw == null || raw.isBlank()) {
            return new String[0];
        }
        // AW2's legacy commands do not consume quoted JSON/NBT; whitespace tokenization
        // therefore preserves their 1.12 behavior.
        return raw.trim().split("\\s+");
    }

    private static final class SenderAdapter implements ICommandSender {
        private final CommandSourceStack source;

        private SenderAdapter(CommandSourceStack source) {
            this.source = source;
        }

        @Override
        public ServerLevel getEntityWorld() {
            return source.getLevel();
        }

        @Override
        public BlockPos getPosition() {
            return BlockPos.containing(source.getPosition());
        }

        @Nullable
        @Override
        public Entity getCommandSenderEntity() {
            return source.getEntity();
        }

        @Override
        public void sendMessage(Component message) {
            source.sendSuccess(() -> message, false);
        }

        @Override
        public String getName() {
            return source.getTextName();
        }

        @Override
        public boolean canUseCommand(int permissionLevel, String commandName) {
            return source.hasPermission(permissionLevel);
        }
    }
}
