package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.shadowmage.ancientwarfare.core.datafixes.ComponentItemFixer;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;
import net.shadowmage.ancientwarfare.structure.tile.ISpecialLootContainer;

import javax.annotation.Nullable;
import java.util.*;

import static net.shadowmage.ancientwarfare.npc.event.EventHandler.GENERATED_INVENTORY_TAG;

public class TemplateRuleBlockInventory extends TemplateRuleBlockTile {

    private static final String INVENTORY_DATA_TAG = "inventoryData";
    public static final String PLUGIN_NAME = "inventory";
    private static final String SIDED_INVENTORIES_TAG = "sidedInventories";
    private static final String LEGACY_FEATURES_TAG = "legacyFeatures";
    private boolean legacyFeatures;
    private int randomLootLevel;
    private Map<Direction, NonNullList<ItemStack>> inventoryStacks = new HashMap<>();

    public TemplateRuleBlockInventory() {
        super();
    }

    public TemplateRuleBlockInventory(Level world, BlockPos pos, BlockState state, int turns) {
        this(world, pos, state, turns, new Direction[]{null}, false);
    }

    public TemplateRuleBlockInventory(Level world, BlockPos pos, BlockState state, int turns, Direction[] sides, boolean legacyFeatures) {
        super(world, pos, state, turns);
        this.legacyFeatures = legacyFeatures;
        WorldTools.getTile(world, pos, BlockEntity.class)
                .ifPresent(te -> {
                    if (te instanceof ISpecialLootContainer && ((ISpecialLootContainer) te).getLootSettings().hasLootToSpawn()) {
                        return;
                    }

                    if (te instanceof ChestBlockEntity) {
                        putInInventoryStacks(null, InventoryTools.getItems(new InvWrapper((ChestBlockEntity) te)));
                        setLegacyRandomLoot(legacyFeatures, null);
                    } else {
                        for (Direction side : sides) {
                            InventoryTools.getItemHandlerFrom(te, side).ifPresent(itemHandler -> {
                                putInInventoryStacks(side, InventoryTools.getItems(itemHandler));
                                setLegacyRandomLoot(legacyFeatures, side);
                            });
                        }
                    }
                });
    }

    private void setLegacyRandomLoot(boolean legacyFeatures, @Nullable Direction side) {
        if (legacyFeatures && side == null && inventoryStacks.get(null).size() == 1) {
            ItemStack keyStack = inventoryStacks.get(null).get(0);
            if (keyStack.getItem() == Items.GOLD_INGOT) {
                this.randomLootLevel = 1;
            } else if (keyStack.getItem() == Items.DIAMOND) {
                this.randomLootLevel = 2;
            } else if (keyStack.getItem() == Items.EMERALD) {
                this.randomLootLevel = 3;
            }
        }
    }

    @Override
    public List<ItemStack> getResources() {
        List<ItemStack> resources = new ArrayList<>(super.getResources());

        for (NonNullList<ItemStack> stacks : inventoryStacks.values()) {
            resources.addAll(stacks);
        }

        return resources;
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        super.handlePlacement(world, turns, pos, builder);
        if (randomLootLevel > 0) {
            for (Map.Entry<Direction, NonNullList<ItemStack>> inventoryEntry : inventoryStacks.entrySet()) {
                WorldTools.getItemHandlerFromTile(world, pos, inventoryEntry.getKey()).ifPresent(itemHandler -> {
                    InventoryTools.emptyInventory(itemHandler);
                    InventoryTools.generateLootFor(world, itemHandler, new Random(world.getRandom().nextLong()), randomLootLevel);
                });
            }
        } else if (legacyFeatures && inventoryStacks.containsKey(null) && !tag.contains("Items")) {
            WorldTools.getItemHandlerFromTile(world, pos, null)
                    .ifPresent(itemHandler -> InventoryTools.insertItems(itemHandler, inventoryStacks.get(null), false));
        }
        WorldTools.getTile(world, pos).ifPresent(te -> te.getPersistentData().putBoolean(GENERATED_INVENTORY_TAG, true));
        BlockTools.notifyBlockUpdate(world, pos);
    }

    @Override
    public boolean shouldReuseRule(Level world, BlockState state, int turns, BlockPos pos) {
        return false;
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        super.writeRuleData(tag);
        tag.putInt("lootLevel", randomLootLevel);
        tag.putBoolean(LEGACY_FEATURES_TAG, legacyFeatures);

        for (Direction side : inventoryStacks.keySet()) {
            if (side == null) {
                writeInventoryForSide(tag, side);
            } else {
                if (!tag.contains(SIDED_INVENTORIES_TAG)) {
                    tag.put(SIDED_INVENTORIES_TAG, new CompoundTag());
                }
                CompoundTag sidedTag = tag.getCompound(SIDED_INVENTORIES_TAG);
                CompoundTag sideTag = new CompoundTag();
                writeInventoryForSide(sideTag, side);
                sidedTag.put(side.getName(), sideTag);
            }
        }
    }

    private void writeInventoryForSide(CompoundTag tag, @Nullable Direction side) {
        NonNullList<ItemStack> stacks = inventoryStacks.get(side);
        CompoundTag invData = new CompoundTag();
        invData.putInt("length", stacks.size());
        CompoundTag itemTag;
        ListTag list = new ListTag();
        ItemStack stack;
        for (int i = 0; i < stacks.size(); i++) {
            stack = stacks.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            itemTag = stack.save(new CompoundTag());
            itemTag.putInt("slot", i);
            list.add(itemTag);
        }
        invData.put("inventoryContents", list);
        tag.put(INVENTORY_DATA_TAG, invData);
    }

    @Override
    public void parseRule(CompoundTag tag) {
        super.parseRule(tag);
        if (tag.contains(INVENTORY_DATA_TAG)) {
            parseInventoryForSide(tag, null);
        }
        if (tag.contains(SIDED_INVENTORIES_TAG)) {
            CompoundTag sidedTag = tag.getCompound(SIDED_INVENTORIES_TAG);
            for (String key : sidedTag.getAllKeys()) {
                Direction side = Direction.byName(key);
                if (side != null) {
                    parseInventoryForSide(sidedTag.getCompound(key), side);
                }
            }
        }
        randomLootLevel = tag.getInt("lootLevel");
        legacyFeatures = !tag.contains(LEGACY_FEATURES_TAG) || tag.getBoolean(LEGACY_FEATURES_TAG);
    }

    private void parseInventoryForSide(CompoundTag tag, @Nullable Direction side) {
        CompoundTag inventoryTag = tag.getCompound(INVENTORY_DATA_TAG);
        int length = inventoryTag.getInt("length");
        NonNullList<ItemStack> stacks = NonNullList.withSize(length, ItemStack.EMPTY);
        CompoundTag itemTag;
        ListTag list = inventoryTag.getList("inventoryContents", Constants.NBT.TAG_COMPOUND);
        int slot;
        ItemStack stack;
        for (int i = 0; i < list.size(); i++) {
            itemTag = list.getCompound(i);
            stack = ItemStack.of(ComponentItemFixer.fixRecursively(itemTag.copy()));
            if (!stack.isEmpty()) {
                slot = itemTag.getInt("slot");
                stacks.set(slot, stack);
            }
        }
        putInInventoryStacks(side, stacks);
    }

    @SuppressWarnings("squid:S1640") //need to use a null key as well which is not supported in EnumMap
    private void putInInventoryStacks(@Nullable Direction side, NonNullList<ItemStack> stacks) {
        inventoryStacks.put(side, stacks);
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
