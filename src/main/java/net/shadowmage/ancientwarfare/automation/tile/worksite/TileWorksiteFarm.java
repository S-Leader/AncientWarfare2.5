package net.shadowmage.ancientwarfare.automation.tile.worksite;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.items.ItemStackHandler;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RelativeSide;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import java.util.*;

public abstract class TileWorksiteFarm extends TileWorksiteBoundedInventory {
    private static final int PLANTABLE_INVENTORY_SIZE = 3;
    private static final int SIZE = 16;
    public static final int DEFAULT_MISC_INVENTORY_SIZE = 3;
    private byte[] targetMap = new byte[SIZE * SIZE];
    private final Queue<BlockPos> blocksToUpdate = new LinkedList<>();
    public final ItemStackHandler plantableInventory;
    public final ItemStackHandler miscInventory;
    protected int plantableCount;
    protected int bonemealCount;

    /*
     * flag should be set to true whenever updating inventory internally (e.g. harvesting blocks) to prevent
     * unnecessary inventory rescanning.  should be set back to false after blocks are added to inventory
     */
    private boolean shouldCountResources;

    public TileWorksiteFarm() {
        super();
        this.shouldCountResources = true;
        plantableInventory = new ItemStackHandler(PLANTABLE_INVENTORY_SIZE) {
            @Override
            protected void onContentsChanged(int slot) {
                shouldCountResources = true;
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return isPlantable(stack) ? super.insertItem(slot, stack, simulate) : stack;
            }
        };
        miscInventory = new ItemStackHandler(getMiscInventorySize()) {
            @Override
            protected void onContentsChanged(int slot) {
                shouldCountResources = true;
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return isMiscItem(stack) ? super.insertItem(slot, stack, simulate) : stack;
            }
        };
        setSideInventory(RelativeSide.FRONT, plantableInventory, RelativeSide.FRONT);
        setSideInventory(RelativeSide.BOTTOM, miscInventory, RelativeSide.TOP);
    }

    protected boolean isMiscItem(ItemStack stack) {
        return isBonemeal(stack);
    }

    protected int getMiscInventorySize() {
        return DEFAULT_MISC_INVENTORY_SIZE;
    }

    protected abstract boolean isPlantable(ItemStack stack);

    protected abstract void scanBlockPosition(BlockPos pos);

    @Override
    protected final void updateWorksite() {
        world.getProfiler().push("Incremental Scan");
        if (blocksToUpdate.isEmpty() && hasWorkBounds()) {
            fillBlocksToProcess(blocksToUpdate);
        }
        if (!blocksToUpdate.isEmpty()) {
            BlockPos pos = blocksToUpdate.poll();
            scanBlockPosition(pos);
        }
        world.getProfiler().pop();
        updateBlockWorksite();
    }

    @Override
    public void onBlockBroken(BlockState state) {
        super.onBlockBroken(state);
        InventoryTools.dropItemsInWorld(world, plantableInventory, pos);
        InventoryTools.dropItemsInWorld(world, miscInventory, pos);
    }

    protected boolean harvestBlock(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (state.isAir()) {
            return false;
        }

        NonNullList<ItemStack> stacks = BlockTools.getDrops(world, pos, state, getFortune());

        if (!inventoryCanHold(stacks)) {
            return false;
        }

        if (!BlockTools.breakBlockNoDrops(world, pos, state)) {
            return false;
        }

        insertOrDropCrops(pos, stacks);
        return true;
    }

    private boolean inventoryCanHold(NonNullList<ItemStack> stacks) {
        List<ItemStack> remainingStacks = InventoryTools.insertItems(plantableInventory, stacks, true);
        remainingStacks = InventoryTools.insertItems(mainInventory, remainingStacks, true);
        return remainingStacks.isEmpty();
    }

    private void insertOrDropCrops(BlockPos pos, NonNullList<ItemStack> stacks) {
        List<ItemStack> remainingItems = InventoryTools.insertItems(plantableInventory, stacks, false);
        InventoryTools.insertOrDropItems(mainInventory, remainingItems, world, pos);
    }

    @Override
    public final boolean userAdjustableBlocks() {
        return true;
    }

    boolean isTarget(BlockPos p) {
        return isTarget(p.getX(), p.getZ());
    }

    private boolean isTarget(int x1, int y1) {
        int z = (y1 - getWorkBoundsMin().getZ()) * SIZE + x1 - getWorkBoundsMin().getX();
        return z >= 0 && z < targetMap.length && targetMap[z] == 1;
    }

    protected boolean isBonemeal(ItemStack stack) {
        return stack.is(Items.BONE_MEAL);
    }

    boolean isFarmable(Block block) {
        return isFarmable(block, new BlockPos(0, 0, 0));
    }

    protected boolean isFarmable(Block block, BlockPos farmablePos) {
        return block instanceof IPlantable;
    }

    protected boolean canReplace(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.canBeReplaced();
    }

    protected boolean tryPlace(ItemStack stack, BlockPos pos, Direction face) {
        return BlockTools.placeItemBlock(stack, world, pos, face);
    }

    private void pickupItems() {
        List<ItemEntity> items = EntityTools.getEntitiesWithinBounds(world, ItemEntity.class, getWorkBoundsMin(), getWorkBoundsMax());
        if (items.isEmpty()) {
            return;
        }
        ItemStack stack;
        for (ItemEntity item : items) {
            if (item.isAlive()) {
                stack = item.getItem();
                if (!stack.isEmpty()) {
                    stack = InventoryTools.mergeItemStack(plantableInventory, stack);
                    stack = InventoryTools.mergeItemStack(mainInventory, stack);
                    if (!stack.isEmpty()) {
                        item.setItem(stack);
                    } else {
                        item.discard();
                    }
                }
            }
        }
    }

    @Override
    protected void validateCollection(Collection<BlockPos> blocks) {
        if (!hasWorkBounds()) {
            blocks.clear();
            return;
        }
        Iterator<BlockPos> it = blocks.iterator();
        BlockPos pos;
        while (it.hasNext() && (pos = it.next()) != null) {
            if (!isInBounds(pos) || !isTarget(pos)) {
                it.remove();
            }
        }
    }

    private void fillBlocksToProcess(Collection<BlockPos> targets) {
        BlockPos min = getWorkBoundsMin();
        BlockPos max = getWorkBoundsMax();
        for (int x = min.getX(); x < max.getX() + 1; x++) {
            for (int z = min.getZ(); z < max.getZ() + 1; z++) {
                if (isTarget(x, z)) {
                    targets.add(new BlockPos(x, min.getY(), z));
                }
            }
        }
    }

    public void onTargetsAdjusted() {
        validateCollection(blocksToUpdate);
        onBoundsAdjusted();
    }

    @Override
    protected void onBoundsSet() {
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                targetMap[z * SIZE + x] = (byte) 1;
            }
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putByteArray("targetMap", targetMap);
        tag.put("plantableInventory", plantableInventory.serializeNBT());
        tag.put("miscInventory", miscInventory.serializeNBT());
        return tag;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        if (tag.contains("targetMap")) {
            targetMap = tag.getByteArray("targetMap");
        }
        plantableInventory.deserializeNBT(tag.getCompound("plantableInventory"));
        miscInventory.deserializeNBT(tag.getCompound("miscInventory"));
    }

    public byte[] getTargetMap() {
        return targetMap;
    }

    public void setTargetBlocks(byte[] targets) {
        boolean change = !Objects.deepEquals(targetMap, targets);
        targetMap = targets;
        if (change) {
            onTargetsAdjusted();
            markDirty();
        }
    }

    private void updateBlockWorksite() {
        world.getProfiler().push("Items Pickup");
        if (world.getDayTime() % 20 == 0) {
            pickupItems();
        }
        world.getProfiler().popPush("Count Resources");
        if (shouldCountResources) {
            countResources();
            shouldCountResources = false;
        }
        world.getProfiler().pop();
    }

    protected void countResources() {
        plantableCount = InventoryTools.getCountOf(plantableInventory, this::isPlantable);
        bonemealCount = InventoryTools.getCountOf(miscInventory, this::isBonemeal);
    }

    protected boolean fertilize(BlockPos pos) {
        Optional<ItemStack> stack = InventoryTools.stream(miscInventory).filter(this::isBonemeal).findFirst();
        if (stack.isPresent() && BoneMealItem.growCrop(stack.get(), world, pos)) {
            world.levelEvent(2005, pos, 0);
            return true;
        }
        return false;
    }
}
