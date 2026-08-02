package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.item.ItemStructureScanner;

import java.util.*;

public class ScannerTracker {
    private ScannerTracker() {
    }

    private static Map<String, Tuple<ResourceKey<Level>, BlockPos>> scannerPositions = new HashMap<>();

    public static void registerScanner(TileStructureScanner scanner) {
        ItemStack scannerStack = scanner.getScannerInventory().getStackInSlot(0);
        String name = ItemStructureScanner.getStructureName(scannerStack);
        if (!name.isEmpty() && !scannerPositions.containsKey(name)) {
            scannerPositions.put(name, new Tuple<>(scanner.getWorld().dimension(), scanner.getPos()));
        }
    }

    public static Set<String> getTrackedScannerNames() {
        return scannerPositions.keySet();
    }

    public static Tuple<ResourceKey<Level>, BlockPos> getScannerPosByName(String name) {
        return scannerPositions.get(name);
    }

    public static void teleportAboveScannerBlock(Player player, Tuple<ResourceKey<Level>, BlockPos> pos) {
        if (player.level().dimension() == pos.getA()) {
            BlockPos aboveScanner = pos.getB().above();
            player.teleportTo(aboveScanner.getX() + 0.5D, aboveScanner.getY(), aboveScanner.getZ() + 0.5D);
        }
    }

    public static void reexportAll(Player player, boolean reloadMainSettings) {
        scannerExporter.startExport(player, reloadMainSettings);
    }

    private static ScannerExporter scannerExporter = new ScannerExporter();

    private static class ScannerExporter {
        private boolean exportFinished = true;
        private boolean teleported = false;
        private int timeout = 0;
        private Set<Tuple<ResourceKey<Level>, BlockPos>> scanners;
        private Iterator<Tuple<ResourceKey<Level>, BlockPos>> exportIterator;
        private Tuple<ResourceKey<Level>, BlockPos> currentPos;
        private Player player;
        private boolean reloadMainSettings;
        private int current = 0;

        private ScannerExporter() {
            MinecraftForge.EVENT_BUS.register(this);
        }

        public void startExport(Player player, boolean reloadMainSettings) {
            this.player = player;
            this.reloadMainSettings = reloadMainSettings;
            exportFinished = false;
            teleported = false;
            timeout = 40;
            current = 0;
            scanners = new HashSet<>(scannerPositions.values());
            exportIterator = scanners.iterator();
        }

        @SubscribeEvent
        public void serverTick(TickEvent.ServerTickEvent evt) {
            if (!exportFinished && evt.phase == TickEvent.Phase.END) {
                if (!teleported) {
                    if (!updatePos()) {
                        return;
                    }
                    teleportAboveScannerBlock(player, currentPos);
                    teleported = true;
                }
                if (timeout <= 0) {
                    WorldTools.getTile(player.level(), currentPos.getB(), TileStructureScanner.class).ifPresent(te -> {
                        if (reloadMainSettings) {
                            te.reloadMainSettings();
                        }
                        te.export();
                        te.getScanner().ifPresent(s ->
                                player.sendSystemMessage(Component.literal("Exported template for " + ItemStructureScanner.getStructureName(s))));
                        player.displayClientMessage(Component.literal(current + " of " + scanners.size() + " templates exported"), true);
                    });
                    timeout = 40;
                    teleported = false;
                } else {
                    timeout--;
                }
            }
        }

        private boolean updatePos() {
            if (!exportIterator.hasNext()) {
                exportFinished = true;
                return false;
            }
            currentPos = exportIterator.next();
            current++;
            while (currentPos.getA() != player.level().dimension()) {
                if (!exportIterator.hasNext()) {
                    exportFinished = true;
                    return false;
                }
                currentPos = exportIterator.next();
                current++;
            }
            return true;
        }
    }

}
