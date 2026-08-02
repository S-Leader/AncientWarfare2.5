package net.shadowmage.ancientwarfare.automation.command;

import net.minecraft.command.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.WarehouseDebugger;

public class CommandWarehouse extends CommandBase {
    @Override
    public String getName() {
        return "awwarehouse";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/awwarehouse startdebug <x> <y> <z> OR /awwarehouse stopdebug [x] [y] [z]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 1) {
            throw new WrongUsageException("/awwarehouse startdebug [x] [y] [z] OR /awwarehouse stopdebug [x] [y] [z]");
        }
        if (args[0].equals("startdebug")) {
            WarehouseDebugger.startTrackingWarehouse(sender.getEntityWorld(), parseBlockPos(sender, args, 1, false));
        } else if (args[0].equals("stopdebug")) {
            WarehouseDebugger.stopTrackingWarehouse(sender.getEntityWorld(), parseBlockPos(sender, args, 1, false));
        }
    }

    public static BlockPos parseBlockPos(ICommandSender sender, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException {
        BlockPos blockpos = sender.getPosition();
        return BlockPos.containing(parseDouble(blockpos.getX(), args[startIndex], -30000000, 30000000, centerBlock), parseDouble(blockpos.getY(), args[startIndex + 1], sender.getEntityWorld().getMinBuildHeight(), sender.getEntityWorld().getMaxBuildHeight() - 1, false), parseDouble(blockpos.getZ(), args[startIndex + 2], -30000000, 30000000, centerBlock));
    }
}
