package net.shadowmage.ancientwarfare.automation.tile.warehouse2;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.automation.AncientWarfareAutomation;
import net.shadowmage.ancientwarfare.core.inventory.ItemHashEntry;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import java.util.*;

public class WarehouseDebugger {
    private static final String DEBUGGER_DATA_NAME = "AWWarehouseDebuggerData";

    @SubscribeEvent
    public void tick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide) {
            getData(event.level).ifPresent(data -> tickData(event.level, data));
        }
    }

    private static void tickData(Level world, WarehouseDebuggerData data) {
        if (data.decreaseAndCheckCooldown()) {
            reviewInventories(world, data);
        }
    }

    private static void reviewInventories(Level world, WarehouseDebuggerData data) {
        Set<BlockPos> invalidWarehouses = new HashSet<>();
        for (BlockPos warehousePos : data.getTrackedWarehouses()) {
            if (world.isLoaded(warehousePos)) {
                Optional<TileWarehouse> wh = WorldTools.getTile(world, warehousePos, TileWarehouse.class);

                if (!wh.isPresent()) {
                    invalidWarehouses.add(warehousePos);
                    continue;
                }

                reviewInventory(wh.get(), data.getItemCounts(warehousePos));
            }
        }

        invalidWarehouses.forEach(data::removeTrackedWarehouse);
    }

    private static void reviewInventory(TileWarehouse warehouse, Map<ItemStack, Integer> debugData) {
        HashMap<ItemStack, Integer> copy = new HashMap<>(debugData);
        for (Map.Entry<ItemHashEntry, Integer> entry : warehouse.cachedItemMap.getItemCounts().entrySet()) {
            reviewStackCount(warehouse.getPos(), debugData, copy, entry);
        }

        for (Map.Entry<ItemStack, Integer> entry : copy.entrySet()) {
            AncientWarfareAutomation.LOG.info("Warehouse {} misses {} of {} that is in debugData", warehouse.getPos(), entry.getValue(),
                    entry.getKey().save(new CompoundTag()).toString());
            debugData.remove(entry.getKey());
        }
    }

    private static void reviewStackCount(BlockPos warehousePos, Map<ItemStack, Integer> debugData, Map<ItemStack, Integer> debugDataCopy, Map.Entry<ItemHashEntry, Integer> warehouseEntry) {
        ItemStack warehouseStack = warehouseEntry.getKey().getItemStack();
        int warehouseCount = warehouseEntry.getValue();

        Iterator<Map.Entry<ItemStack, Integer>> it = debugDataCopy.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ItemStack, Integer> entry = it.next();
            if (InventoryTools.doItemStacksMatch(warehouseStack, entry.getKey())) {
                if (warehouseCount != entry.getValue()) {
                    AncientWarfareAutomation.LOG.info("Warehouse {} has {} {} of {} than what's tracked in debug data", warehousePos,
                            Math.abs(entry.getValue() - warehouseCount), warehouseCount > entry.getValue() ? "more" : "less",
                            warehouseStack.save(new CompoundTag()).toString());
                    debugData.put(entry.getKey(), warehouseCount);
                }
                it.remove();
                return;
            }
        }

        AncientWarfareAutomation.LOG.info("Warehouse {} has {} of {} which is missing in debug data", warehousePos, warehouseCount,
                warehouseStack.save(new CompoundTag()).toString());
        debugData.put(warehouseStack, warehouseCount);
    }

    public static void startTrackingWarehouse(Level world, BlockPos pos) {
        if (!world.isLoaded(pos)) {
            return;
        }
        getData(world).ifPresent(data -> {
            data.addTrackedWarehouse(pos);
            initializeItemCounts(world, pos, data);
        });
    }

    private static void initializeItemCounts(Level world, BlockPos pos, WarehouseDebuggerData debugData) {
        WorldTools.getTile(world, pos, TileWarehouse.class).ifPresent((TileWarehouse wh) -> {
            Map<ItemStack, Integer> warehouseItems = new HashMap<>();
            for (Map.Entry<ItemHashEntry, Integer> itemCount : wh.cachedItemMap.getItemCounts().entrySet()) {
                warehouseItems.put(itemCount.getKey().getItemStack().copy(), itemCount.getValue());
            }
            debugData.setItemCounts(pos, warehouseItems);
        });
    }

    public static void stopTrackingWarehouse(Level world, BlockPos pos) {
        getData(world).ifPresent(data -> data.removeTrackedWarehouse(pos));
    }

    public static void removeItem(Level world, BlockPos pos, ItemStack stack, int count) {
        getData(world).ifPresent(data -> data.removeItem(pos, stack, count));
    }

    public static void addItem(Level world, BlockPos pos, ItemStack stack, int count) {
        getData(world).ifPresent(data -> data.addItem(pos, stack, count));
    }

    public static void reinitializeItemCounts(Level world, BlockPos pos) {
        getData(world).ifPresent(data -> {
            if (data.isTrackingWarehouse(pos)) {
                data.removeTrackedWarehouse(pos);
                data.addTrackedWarehouse(pos);
                initializeItemCounts(world, pos, data);
            }
        });
    }

    private static Optional<WarehouseDebuggerData> getData(Level world) {
        return WorldTools.getWorldSavedData(world, WarehouseDebuggerData.class, DEBUGGER_DATA_NAME, true);
    }

    public static void changeItemQuantity(Level world, BlockPos pos, ItemStack stack, int change) {
        getData(world).ifPresent(data -> {
            if (change > 0) {
                data.addItem(pos, stack, change);
            } else if (change < 0) {
                data.removeItem(pos, stack, -change);
            }
        });
    }
}
