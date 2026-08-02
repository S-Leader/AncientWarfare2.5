package net.minecraft.command;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

/**
 * Small 1.12 command-source facade backed by a 1.20.1 CommandSourceStack.
 * It exists only to keep AW2's sub-command tree intact while registration is
 * delegated to Brigadier.
 */
public interface ICommandSender {
    ServerLevel getEntityWorld();

    BlockPos getPosition();

    @Nullable
    Entity getCommandSenderEntity();

    void sendMessage(Component message);

    String getName();

    boolean canUseCommand(int permissionLevel, String commandName);
}
