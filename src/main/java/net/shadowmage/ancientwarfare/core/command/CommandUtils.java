package net.shadowmage.ancientwarfare.core.command;

import com.google.common.collect.AbstractIterator;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketManualReload;
import net.shadowmage.ancientwarfare.core.util.FileUtils;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandUtils extends RootCommand {
    public CommandUtils() {
        registerSubCommand(new EntityListCommand());
        registerSubCommand(new EntityListCommand());
        registerSubCommand(new BiomeListCommand());
        registerSubCommand(new BlockListCommand());
        registerSubCommand(new ReloadManualCommand());
        registerSubCommand(new LootTableListCommand());
        registerSubCommand(new ChunkLoadCommand());
    }

    @Override
    public String getName() {
        return "awutils";
    }

    private abstract static class ExportCommand implements ISubCommand {
        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
            List<String> lines = getLines(server);
            String fileName = args.length > 0 ? args[0] : getDefaultFileName();
            String filePath = AWCoreStatics.utilsExportPath;
            File file = new File(filePath, fileName);
            exportToFile(file, getHeader(), lines);
            notifyPlayer(sender, file);
        }

        protected abstract String getHeader();

        protected abstract String getDefaultFileName();

        protected abstract List<String> getLines(MinecraftServer server);

        private static void exportToFile(File exportFile, String header, List<String> data) {
            ArrayList<String> rows = new ArrayList<>();
            rows.add(header);
            rows.addAll(data);
            FileUtils.exportToFile(exportFile, rows);
        }

        private static void notifyPlayer(ICommandSender sender, File exportFile) {
            sender.sendMessage(Component.literal("File exported to " + exportFile.getAbsoluteFile()));
        }

        @Override
        public int getMaxArgs() {
            return 1;
        }
    }

    private static class EntityListCommand extends ExportCommand {
        @Override
        protected String getHeader() {
            return "Registry Name,Entity Name,Entity Class";
        }

        @Override
        protected String getDefaultFileName() {
            return "entitylist.csv";
        }

        @Override
        protected List<String> getLines(MinecraftServer server) {
            return ForgeRegistries.ENTITY_TYPES.getEntries().stream()
                    .map(entry -> String.join(",", entry.getKey().location().toString(), entry.getValue().getDescriptionId(), entry.getValue().getClass().getName()))
                    .sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        }

        @Override
        public String getName() {
            return "exportentities";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return getName() + " [fileName - defaults to \"entitylist.csv\"]";
        }
    }

    private static class BiomeListCommand extends ExportCommand {
        @Override
        protected String getHeader() {
            return "Registry Name,Biome Name,Base Temperature,Has Precipitation,Downfall,Biome Tags,Biome Class";
        }

        @Override
        protected String getDefaultFileName() {
            return "biomelist.csv";
        }

        @Override
        protected List<String> getLines(MinecraftServer server) {
            //noinspection ConstantConditions
            return ForgeRegistries.BIOMES.getValues().stream()
                    .map(b -> String.join(",", ForgeRegistries.BIOMES.getKey(b).toString(), getBiomeName(b), Float.toString(b.getBaseTemperature())
                            , Boolean.toString(b.hasPrecipitation()), Float.toString(b.getModifiedClimateSettings().downfall())
                            , getBiomeTags(b),
                            b.getClass().getName()))
                    .sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        }

        private String getBiomeName(Biome b) {
            ResourceLocation key = ForgeRegistries.BIOMES.getKey(b);
            return key == null ? "" : key.getPath();
        }

        private String getBiomeTags(Biome b) {
            ITagManager<Biome> tags = ForgeRegistries.BIOMES.tags();
            if (tags == null) {
                return "";
            }
            return tags.getReverseTag(b)
                    .map(reverseTag -> reverseTag.getTagKeys().map(t -> t.location().toString()).collect(Collectors.joining("|")))
                    .orElse("");
        }

        @Override
        public String getName() {
            return "exportbiomes";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return getName() + " [fileName - defaults to \"biomelist.csv\"]";
        }
    }

    private static class BlockListCommand extends ExportCommand {

        @Override
        protected String getHeader() {
            return "Registry Name,Block Name,Skippable,Skippable Material,Target,Target Material";
        }

        @Override
        protected String getDefaultFileName() {
            return "blocklist.csv";
        }

        @Override
        protected List<String> getLines(MinecraftServer server) {
            //noinspection ConstantConditions
            return ForgeRegistries.BLOCKS.getValues().stream()
                    .map(b -> String.join(",", ForgeRegistries.BLOCKS.getKey(b).toString(), b.getName().getString(),
                            AWStructureStatics.isSkippable(b.defaultBlockState()) ? "Y" : "N",
                            AWStructureStatics.isSkippableMaterial(LegacyMaterial.of(b.defaultBlockState())) ? "Y" : "N",
                            AWStructureStatics.isValidTargetBlock(b.defaultBlockState()) ? "Y" : "N",
                            AWStructureStatics.isValidTargetMaterial(LegacyMaterial.of(b.defaultBlockState())) ? "Y" : "N"
                    )).sorted(Comparator.naturalOrder()).collect(Collectors.toList());
        }

        @Override
        public String getName() {
            return "exportblocks";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return getName() + " [fileName - defaults to \"blocklist.csv\"]";
        }
    }

    private static class ReloadManualCommand implements ISubCommand {
        @Override
        public String getName() {
            return "reloadmanual";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
            Entity senderEntity = sender.getCommandSenderEntity();
            if (senderEntity instanceof Player) {
                NetworkHandler.sendToPlayer((ServerPlayer) senderEntity, new PacketManualReload());
            }
        }

        @Override
        public int getMaxArgs() {
            return 0;
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return getName();
        }
    }

    private static class LootTableListCommand extends ExportCommand {
        @Override
        protected String getHeader() {
            return "Registry Name";
        }

        @Override
        protected String getDefaultFileName() {
            return "loottablelist.csv";
        }

        @Override
        protected List<String> getLines(MinecraftServer server) {
            return server.getLootData().getKeys(LootDataType.TABLE).stream().map(ResourceLocation::toString).collect(Collectors.toList());
        }

        @Override
        public String getName() {
            return "exportloottables";
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return getName() + " [fileName - defaults to \"loottablelist.csv\"]";
        }
    }

    private static class ChunkLoadCommand implements ISubCommand {
        private PlayerMover playerMover = new PlayerMover();

        private ChunkLoadCommand() {
            MinecraftForge.EVENT_BUS.register(playerMover);
        }

        @Override
        public String getName() {
            return "loadChunks";
        }

        @Override
        public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
            if (args.length < 1) {
                throw new WrongUsageException(getUsage(sender));
            }
            if (!(sender.getCommandSenderEntity() instanceof ServerPlayer)) {
                return;
            }
            int chunkLoadRadius = server.getPlayerList().getViewDistance();
            int range = Integer.parseInt(args[0]);

            playerMover.startMoving((ServerPlayer) sender.getCommandSenderEntity(), sender.getEntityWorld(), chunkLoadRadius, range);
        }

        @Override
        public int getMaxArgs() {
            return 1;
        }

        @Override
        public String getUsage(ICommandSender sender) {
            return getName() + " <diameterInChunks>";
        }

        //this is not made for multiple players using it on server as there's likely no need for that
        private static class PlayerMover {
            private ServerPlayer player;
            private int chunkLoadRadius;
            private int range;
            private BlockPos originalPosition;
            private ChunkPos originalChunkPos;
            private boolean finishedMoving = true;
            private Iterator<ChunkPos> iterator;
            private int timeout = 0;

            @SuppressWarnings("unused") // used in event listener reflection
            @SubscribeEvent
            public void serverTick(TickEvent.ServerTickEvent evt) {
                if (evt.phase == TickEvent.Phase.END) {
                    if (timeout <= 0) {
                        movePlayer();
                        timeout = 200;
                    } else {
                        timeout--;
                    }
                }
            }

            private void startMoving(ServerPlayer player, Level world, int chunkLoadRadius, int range) {
                this.player = player;
                originalPosition = player.blockPosition();
                originalChunkPos = world.getChunkAt(originalPosition).getPos();
                this.chunkLoadRadius = chunkLoadRadius;
                this.range = range;
                finishedMoving = false;
                iterator = getAllChunkPosStops();
            }

            private Iterator<ChunkPos> getAllChunkPosStops() {
                return new AbstractIterator<ChunkPos>() {
                    private boolean first = true;
                    private int currentX;
                    private int currentZ;

                    @Override
                    protected ChunkPos computeNext() {
                        if (first) {
                            currentX = getInitialX();
                            currentZ = getInitialZ();
                            first = false;
                        } else if (currentX + chunkLoadRadius >= originalChunkPos.x + range && currentZ + chunkLoadRadius >= originalChunkPos.z + range) {
                            return endOfData();
                        } else {
                            if (currentX + chunkLoadRadius < originalChunkPos.x + range) {
                                currentX += 2 * chunkLoadRadius;
                            } else if (currentZ + chunkLoadRadius < originalChunkPos.z + range) {
                                currentX = getInitialX();
                                currentZ += 2 * chunkLoadRadius;
                            }
                        }
                        return new ChunkPos(currentX, currentZ);
                    }

                    private int getInitialZ() {
                        return originalChunkPos.z - range + chunkLoadRadius;
                    }

                    private int getInitialX() {
                        return originalChunkPos.x - range + chunkLoadRadius;
                    }
                };
            }

            private void movePlayer() {
                if (!finishedMoving) {
                    if (!iterator.hasNext()) {
                        player.connection.teleport(originalPosition.getX(), originalPosition.getY(), originalPosition.getZ(), player.getYRot(), player.getXRot());
                        finishedMoving = true;
                        return;
                    }
                    ChunkPos chunkPos = iterator.next();
                    player.connection.teleport(chunkPos.getMinBlockX() + 8d, 255, chunkPos.getMinBlockZ() + 8d, player.getYRot(), player.getXRot());
                }
            }
        }
    }
}
