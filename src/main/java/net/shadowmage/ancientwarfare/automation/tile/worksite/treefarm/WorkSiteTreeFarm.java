package net.shadowmage.ancientwarfare.automation.tile.worksite.treefarm;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.shadowmage.ancientwarfare.automation.registry.TreeFarmRegistry;
import net.shadowmage.ancientwarfare.automation.tile.worksite.IWorksiteAction;
import net.shadowmage.ancientwarfare.automation.tile.worksite.TileWorksiteFarm;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.entity.AWFakePlayer;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class WorkSiteTreeFarm extends TileWorksiteFarm {
    private static final int MAX_DISTANCE_TO_INITIAL_CUT_POSITION = 15;
    private static final String TARGET_LIST_TAG = "targetList";
    private static final String TARGET_LEAF_LIST_TAG = "targetLeafList";
    private boolean hasShears;
    private final Set<BlockPos> blocksToShear = new LinkedHashSet<>();
    private final Set<BlockPos> leafBlocksToChop = new LinkedHashSet<>();
    private final Set<BlockPos> trunkBlocksToChop = new LinkedHashSet<>();
    private final Set<BlockPos> blocksToPlant = new LinkedHashSet<>();
    private final Set<BlockPos> blocksToFertilize = new LinkedHashSet<>();

    private final IItemHandler inventoryForDrops;

    public WorkSiteTreeFarm() {
        super();
        inventoryForDrops = new CombinedInvWrapper(plantableInventory, mainInventory);
    }

    @Override
    protected boolean isPlantable(ItemStack stack) {
        return TreeFarmRegistry.isPlantable(stack);
    }

    @Override
    protected boolean isMiscItem(ItemStack stack) {
        return stack.getItem() == Items.SHEARS || super.isMiscItem(stack);
    }

    @Override
    public void onBoundsAdjusted() {
        validateCollection(blocksToFertilize);
        validateCollection(trunkBlocksToChop);
        validateCollection(leafBlocksToChop);
        validateCollection(blocksToPlant);
        if (!hasShears) {
            blocksToShear.clear();
        }
        markDirty();
    }

    @Override
    protected void countResources() {
        super.countResources();
        hasShears = InventoryTools.getCountOf(miscInventory, s -> s.getItem() == Items.SHEARS) > 0;
    }

    private boolean bonemealBlock() {
        if (bonemealCount <= 0 || blocksToFertilize.isEmpty()) {
            return false;
        }

        Iterator<BlockPos> it = blocksToFertilize.iterator();
        BlockPos position = it.next();
        it.remove();

        BlockState state = world.getBlockState(position);

        return canFertilize(world, position, state) && fertilize(position);

    }

    private boolean plant() {
        if (plantableCount <= 0 || blocksToPlant.isEmpty()) {
            return false;
        }

        //noinspection ConstantConditions
        Optional<Tuple<ItemStack, ISapling>> plantable = InventoryTools.stream(plantableInventory).map(p -> new Tuple<>(p, TreeFarmRegistry.getSapling(p)))
                .filter(t -> t.getB().isPresent()).map(t -> new Tuple<>(t.getA(), t.getB().get())).findFirst();
        if (plantable.isPresent()) {
            Iterator<BlockPos> it = blocksToPlant.iterator();
            while (it.hasNext()) {
                BlockPos position = it.next();
                it.remove();

                ItemStack stack = plantable.get().getA();
                ISapling sapling = plantable.get().getB();
                if (canReplace(position) && tryPlantingSapling(position, stack, sapling)) {
                    InventoryTools.removeItems(plantableInventory, stack, 1);
                    return true;
                }
            }
        }

        return false;
    }

    private boolean tryPlantingSapling(BlockPos position, ItemStack stack, ISapling sapling) {
        return sapling.isRightClick() ? BlockTools.placeItemBlockRightClick(stack.copy(), world, position) :
                tryPlace(stack.copy(), position, Direction.UP) || tryPlace(stack.copy(), position, Direction.DOWN);
    }

    private boolean chopBlock(boolean wood) {
        if ((wood && trunkBlocksToChop.isEmpty()) || (!wood && leafBlocksToChop.isEmpty())) {
            return false;
        }

        Iterator<BlockPos> it = wood ? trunkBlocksToChop.iterator() : leafBlocksToChop.iterator();
        BlockPos position = it.next();
        BlockState state = world.getBlockState(position);
        if (LegacyMaterial.of(state) == LegacyMaterial.AIR) {
            it.remove();
            return false;
        }

        IBlockExtraDrop extraDrop = TreeFarmRegistry.getBlockExtraDrop(state);
        NonNullList<ItemStack> extraDrops = extraDrop.getDrops(world, position, state, getFortune());
        if (!harvestBlock(position)) {
            return false;
        }
        it.remove();
        InventoryTools.insertOrDropItems(inventoryForDrops, extraDrops, world, position);
        return true;
    }

    private boolean shearBlock() {
        if (!hasShears || blocksToShear.isEmpty()) {
            return false;
        }

        Iterator<BlockPos> it = blocksToShear.iterator();
        BlockPos position = it.next();
        it.remove();
        Block block = world.getBlockState(position).getBlock();
        if (block instanceof IForgeShearable shearable) {
            Optional<ItemStack> shears = InventoryTools.stream(miscInventory).filter(s -> s.getItem() instanceof ShearsItem).findFirst();

            if (shears.isPresent() && shear(position, shearable, shears.get())) {
                return true;
            }
        }

        return false;
    }

    private boolean shear(BlockPos position, IForgeShearable block, ItemStack shears) {
        if (block.isShearable(shears, world, position)) {
            List<ItemStack> drops = block.onSheared(AWFakePlayer.get(world), shears, world, position, getFortune());
            drops = InventoryTools.insertItems(plantableInventory, drops, false);
            InventoryTools.insertOrDropItems(mainInventory, drops, world, pos);
            world.removeBlock(position, false);
            return true;
        }
        return false;
    }

    private void addTreeBlocks(BlockState state, BlockPos basePos) {
        world.getProfiler().push("TreeFinder");

        ITree tree = TreeFarmRegistry.getTreeScanner(state).scanTree(world, basePos,
                basePos.offset(-MAX_DISTANCE_TO_INITIAL_CUT_POSITION, 0, -MAX_DISTANCE_TO_INITIAL_CUT_POSITION),
                basePos.offset(MAX_DISTANCE_TO_INITIAL_CUT_POSITION, 0, MAX_DISTANCE_TO_INITIAL_CUT_POSITION));
        List<BlockPos> leafBlocks = tree.getLeafPositions();
        if (hasShears) {
            blocksToShear.addAll(leafBlocks);
        } else {
            leafBlocksToChop.addAll(leafBlocks);
        }
        List<BlockPos> trunkBlocks = tree.getTrunkPositions();
        trunkBlocksToChop.addAll(trunkBlocks);

        if (!leafBlocks.isEmpty() || !trunkBlocks.isEmpty()) {
            markDirty();
        }
        world.getProfiler().pop();
    }

    @Override
    public WorkType getWorkType() {
        return WorkType.FORESTRY;
    }

    @Override
    public boolean onBlockClicked(Player player, @Nullable InteractionHand hand) {
        if (!player.level().isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_WORKSITE_TREE_FARM, pos);
        }
        return true;
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (!trunkBlocksToChop.isEmpty()) {
            ListTag chopList = new ListTag();
            for (BlockPos position : trunkBlocksToChop) {
                chopList.add(LongTag.valueOf(position.asLong()));
            }
            tag.put(TARGET_LIST_TAG, chopList);
        }
        if (!leafBlocksToChop.isEmpty()) {
            ListTag chopList = new ListTag();
            for (BlockPos position : leafBlocksToChop) {
                chopList.add(LongTag.valueOf(position.asLong()));
            }
            tag.put(TARGET_LEAF_LIST_TAG, chopList);
        }
        return tag;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        trunkBlocksToChop.clear();
        if (tag.contains(TARGET_LIST_TAG)) {
            ListTag chopList = tag.getList(TARGET_LIST_TAG, Constants.NBT.TAG_LONG);
            for (int i = 0; i < chopList.size(); i++) {
                trunkBlocksToChop.add(BlockPos.of(((LongTag) chopList.get(i)).getAsLong()));
            }
        }
        if (tag.contains(TARGET_LEAF_LIST_TAG)) {
            ListTag chopList = tag.getList(TARGET_LEAF_LIST_TAG, Constants.NBT.TAG_LONG);
            for (int i = 0; i < chopList.size(); i++) {
                leafBlocksToChop.add(BlockPos.of(((LongTag) chopList.get(i)).getAsLong()));
            }
        }
    }

    @Override
    protected void scanBlockPosition(BlockPos scanPos) {
        if (canReplace(scanPos)) {
            BlockState state = world.getBlockState(scanPos.below());
            if (TreeFarmRegistry.isSoil(state) || (state.canBeReplaced() && TreeFarmRegistry
                    .isSoil(world.getBlockState(scanPos.above())))) {
                blocksToPlant.add(scanPos);
            }
        } else {
            BlockState state = world.getBlockState(scanPos);
            if (canFertilize(world, scanPos, state)) {
                blocksToFertilize.add(scanPos);
            } else if (LegacyMaterial.of(state) != LegacyMaterial.AIR && trunkBlocksToChop.isEmpty() && leafBlocksToChop.isEmpty()) {
                addTreeBlocks(state, scanPos);
            }
        }
    }

    private boolean canFertilize(Level world, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof BonemealableBlock growable && growable.isValidBonemealTarget(world, pos, state, world.isClientSide);
    }

    private static final IWorksiteAction SHEAR_ACTION = WorksiteImplementation::getEnergyPerActivation;
    private static final IWorksiteAction CHOP_TRUNK_ACTION = e -> WorksiteImplementation.getEnergyPerActivation(e) / 2D;
    private static final IWorksiteAction CHOP_LEAF_ACTION = e -> WorksiteImplementation.getEnergyPerActivation(e) / 10D;
    private static final IWorksiteAction PLANT_ACTION = WorksiteImplementation::getEnergyPerActivation;
    private static final IWorksiteAction BONEMEAL_ACTION = WorksiteImplementation::getEnergyPerActivation;

    @Override
    protected Optional<IWorksiteAction> getNextAction() {
        if (hasShears && !blocksToShear.isEmpty()) {
            return Optional.of(SHEAR_ACTION);
        } else if (!leafBlocksToChop.isEmpty()) {
            return Optional.of(CHOP_LEAF_ACTION);
        } else if (!trunkBlocksToChop.isEmpty()) {
            return Optional.of(CHOP_TRUNK_ACTION);
        } else if (plantableCount > 0 && !blocksToPlant.isEmpty()) {
            return Optional.of(PLANT_ACTION);
        } else if (bonemealCount > 0 && !blocksToFertilize.isEmpty()) {
            return Optional.of(BONEMEAL_ACTION);
        }
        return Optional.empty();
    }

    @Override
    protected boolean processAction(IWorksiteAction action) {
        if (action == SHEAR_ACTION) {
            return shearBlock();
        } else if (action == CHOP_TRUNK_ACTION) {
            return chopBlock(true);
        } else if (action == CHOP_LEAF_ACTION) {
            return chopBlock(false);
        } else if (action == BONEMEAL_ACTION) {
            return bonemealBlock();
        } else if (action == PLANT_ACTION) {
            return plant();
        }
        return false;
    }
}
