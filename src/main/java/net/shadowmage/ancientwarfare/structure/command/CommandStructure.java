package net.shadowmage.ancientwarfare.structure.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.command.ISubCommand;
import net.shadowmage.ancientwarfare.core.command.RootCommand;
import net.shadowmage.ancientwarfare.core.command.SimpleSubCommand;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureEntry;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureMap;
import net.shadowmage.ancientwarfare.structure.item.ItemStructureScanner;
import net.shadowmage.ancientwarfare.structure.item.ItemStructureSettings;
import net.shadowmage.ancientwarfare.structure.network.PacketShowBoundingBoxes;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplateManager;
import net.shadowmage.ancientwarfare.structure.template.WorldGenStructureManager;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuilder;
import net.shadowmage.ancientwarfare.structure.template.load.TemplateLoader;
import net.shadowmage.ancientwarfare.structure.tile.ScannerTracker;
import net.shadowmage.ancientwarfare.structure.worldgen.TerritoryManager;
import net.shadowmage.ancientwarfare.structure.worldgen.WorldGenDetailedLogHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CommandStructure extends RootCommand {
    public CommandStructure() {
        registerSubCommand(new DeleteCommand());
        registerSubCommand(new BuildCommand());
        registerSubCommand(new SaveCommand());
        registerSubCommand(new SimpleSubCommand("reload",
                (server, sender, args) -> {
                    WorldGenStructureManager.INSTANCE.clearCachedTemplates();
                    TerritoryManager.clearTerritoryCache();
                    WorldGenStructureManager.INSTANCE.loadBiomeList(); //reset biome to template cache
                    TemplateLoader.INSTANCE.reloadAll();
                    sender.sendMessage(Component.translatable("command.aw.structure.reloaded"));
                }));
        registerSubCommand(new ReexportCommand());
        registerSubCommand(new SimpleSubCommand("scannerTp", (server, sender, args) -> {
            if (sender.getCommandSenderEntity() instanceof Player player) {
                Tuple<ResourceKey<Level>, BlockPos> pos = ScannerTracker.getScannerPosByName(args[0]);
                ScannerTracker.teleportAboveScannerBlock(player, pos);
            }


        }) {
            @Override
            public int getMaxArgs() {
                return 1;
            }

            @Override
            public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
                return CommandBase.getListOfStringsMatchingLastWord(args, ScannerTracker.getTrackedScannerNames().toArray(new String[0]));
            }

            @Override
            public String getUsage(ICommandSender sender) {
                return getName() + " <scannerTemplateName>";
            }
        });
        registerSubCommand(new SimpleSubCommand("name", (server, sender, args) -> {
            Optional<StructureEntry> structure = AWGameData.INSTANCE.getPerWorldData(sender.getEntityWorld(), StructureMap.class)
                    .getStructureAt(sender.getEntityWorld(), sender.getPosition());

            sender.sendMessage(structure.map(structureEntry -> Component.translatable("command.aw.structure.name", structureEntry.getName()))
                    .orElseGet(() -> Component.translatable("command.aw.structure.no_structure")));
        }));
        registerSubCommand(new SimpleSubCommand("showBoundingBoxes", (server, sender, args) -> {
            if (sender.getCommandSenderEntity() instanceof ServerPlayer player) {
                NetworkHandler.sendToPlayer(player, new PacketShowBoundingBoxes(true));
            }
        }));
        registerSubCommand(new SimpleSubCommand("hideBoundingBoxes", (server, sender, args) -> {
            if (sender.getCommandSenderEntity() instanceof ServerPlayer player) {
                NetworkHandler.sendToPlayer(player, new PacketShowBoundingBoxes(false));
            }
        }));
        registerSubCommand(new SimpleSubCommand("worldgenDebug", (server, sender, args) -> {
            if (!"true".equalsIgnoreCase(args[0]) && !"false".equalsIgnoreCase(args[0])) {
                throw new WrongUsageException("worldgenDebug <true|false>");
            }
            WorldGenDetailedLogHelper.shouldLogValidationMessages = Boolean.parseBoolean(args[0]);
            sender.sendMessage(Component.literal("Detailed world-generation logging: " + (WorldGenDetailedLogHelper.shouldLogValidationMessages ? "enabled" : "disabled")));
        }) {
            @Override
            public int getMaxArgs() {
                return 1;
            }

            @Override
            public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
                return CommandBase.getListOfStringsMatchingLastWord(args, "true", "false");
            }

            @Override
            public String getUsage(ICommandSender sender) {
                return "worldgenDebug <true|false>";
            }
        });
        registerSubCommand(new StatsCommand());
    }

    @Override
    public String getName() {
        return "awstructure";
    }

    @Override
    public String getUsage(ICommandSender var1) {
        return "command.aw.structure.usage";
    }

    private static class SaveCommand implements ISubCommand {
        @Override
        public String getName() {
            return "save";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] subArgs) {
            if (sender.getCommandSenderEntity() instanceof LivingEntity living) {
                ItemStack stack = living.getMainHandItem();
                if (!stack.isEmpty()) {
                    ItemStructureSettings settings = ItemStructureSettings.getSettingsFor(stack);
                    if (settings.hasPos1() && settings.hasPos2() && settings.hasBuildKey() && (settings.hasName() || subArgs.length > 0)) {
                        scanStructure(sender, subArgs, stack, settings);
                    } else {
                        sender.sendMessage(Component.translatable("command.aw.structure.incomplete_data"));
                    }
                }
            }
        }

        private void scanStructure(ICommandSender sender, String[] subArgs, ItemStack stack, ItemStructureSettings settings) {
            String name = settings.hasName() ? settings.name() : subArgs[0];
            ItemStructureScanner.setStructureName(stack, name);
            if (ItemStructureScanner.scanStructure(sender.getEntityWorld(), stack)) {
                sender.sendMessage(Component.translatable("command.aw.structure.exported", subArgs[0]));
            }
        }

        @Override
        public int getMaxArgs() {
            return 1;
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return getName() + " [templateName]";
        }
    }

    private static class BuildCommand implements ISubCommand {
        @Override
        public String getName() {
            return "build";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] subArgs) throws CommandException {
            if (subArgs.length < 4) {
                throw new WrongUsageException(getUsage(sender));
            }

            int x = CommandBase.parseInt(subArgs[1]);
            int y = CommandBase.parseInt(subArgs[2]);
            int z = CommandBase.parseInt(subArgs[3]);
            Direction face = Direction.SOUTH;
            if (subArgs.length > 4) {
                Direction faceArg = Direction.byName(subArgs[4]);
                if (faceArg != null) {
                    face = faceArg;
                }
            }
            Optional<StructureTemplate> template = StructureTemplateManager.getTemplate(subArgs[0]);
            if (template.isPresent()) {
                StructureBuilder builder = new StructureBuilder(sender.getEntityWorld(), template.get(), face, new BlockPos(x, y, z));
                builder.instantConstruction();
                sender.sendMessage(Component.translatable("command.aw.structure.built", subArgs[0], x, y, z));
            } else {
                sender.sendMessage(Component.translatable("command.aw.structure.not_found", subArgs[0]));
            }
        }

        @Override
        public int getMaxArgs() {
            return 5;
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return getName() + " <templateName> <x> <y> <z> [north:east:south:west]";
        }

        @Override
        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
            return args.length > 4 ? CommandBase.getListOfStringsMatchingLastWord(args, "north", "east", "south", "west") : Collections.emptyList();
        }
    }

    private static class DeleteCommand implements ISubCommand {
        @Override
        public String getName() {
            return "delete";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] subArgs) throws CommandException {
            if (subArgs.length < 1) {
                throw new WrongUsageException(getUsage(sender));
            }
            String name = subArgs[0];
            boolean flag = StructureTemplateManager.removeTemplate(name);
            if (flag)//check if var2.len>=3, pull string of end...if string==true, try delete template file for name
            {
                Component txt = Component.translatable("command.aw.structure.template_removed", name);
                sender.sendMessage(txt);
                if (subArgs.length > 1) {
                    boolean shouldDelete = subArgs[1].equalsIgnoreCase("true");
                    if (shouldDelete) {
                        if (deleteTemplateFile(name)) {
                            txt = Component.translatable("command.aw.structure.file_deleted", name);
                        } else {
                            txt = Component.translatable("command.aw.structure.file_not_found", name);
                        }
                        sender.sendMessage(txt);
                    }
                }
            } else//send template not found message
            {
                sender.sendMessage(Component.translatable("command.aw.structure.not_found", name));
            }
        }

        private boolean deleteTemplateFile(String name) {
            String path = TemplateLoader.INCLUDE_DIRECTORY + name + "." + AWStructureStatics.templateExtension;
            File file = new File(path);
            if (file.exists()) {
                try {
                    Files.delete(file.toPath());
                } catch (IOException e) {
                    AncientWarfareStructure.LOG.error("Unable to delete file ", e);
                }
                return true;
            }
            return false;
        }

        @Override
        public int getMaxArgs() {
            return 2;
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return getName() + " <templateName> [true to remove the aws file]";
        }
    }

    private static class ReexportCommand implements ISubCommand {
        @Override
        public String getName() {
            return "scannersReexport";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
            if (sender.getCommandSenderEntity() instanceof Player player) {
                boolean reloadMainSettings = false;
                if (args.length == 1) {
                    reloadMainSettings = Boolean.parseBoolean(args[0]);
                }
                ScannerTracker.reexportAll(player, reloadMainSettings);
            }
        }

        @Override
        public int getMaxArgs() {
            return 1;
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return getName() + " [true to reload main settings]";
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }
}
