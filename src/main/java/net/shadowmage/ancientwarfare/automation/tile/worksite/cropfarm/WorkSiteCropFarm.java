package net.shadowmage.ancientwarfare.automation.tile.worksite.cropfarm;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.shadowmage.ancientwarfare.automation.registry.CropFarmRegistry;
import net.shadowmage.ancientwarfare.automation.tile.worksite.IWorksiteAction;
import net.shadowmage.ancientwarfare.automation.tile.worksite.TileWorksiteFarm;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class WorkSiteCropFarm extends TileWorksiteFarm {
    private final Set<BlockPos> blocksToTill = new LinkedHashSet<>();
    private final Set<BlockPos> blocksToHarvest = new LinkedHashSet<>();
    private final Set<BlockPos> blocksToPlant = new LinkedHashSet<>();
    private final Set<BlockPos> blocksToFertilize = new LinkedHashSet<>();

    private final IItemHandler inventoryForDrops;

    public WorkSiteCropFarm() {
        super();
        inventoryForDrops = new CombinedInvWrapper(plantableInventory, mainInventory);
    }

    @Override
    protected boolean isPlantable(ItemStack stack) {
        return CropFarmRegistry.getCrop(stack).isPlantable(stack);
    }

    @Override
    protected boolean isFarmable(Block block, BlockPos farmablePos) {
        if (super.isFarmable(block, farmablePos)) {
            return ((IPlantable) block).getPlantType(world, farmablePos) == PlantType.CROP;
        }
        return block instanceof CropBlock || block instanceof StemBlock;
    }

    @Override
    public void onBoundsAdjusted() {
        validateCollection(blocksToFertilize);
        validateCollection(blocksToHarvest);
        validateCollection(blocksToPlant);
        validateCollection(blocksToTill);
    }

    @Override
    protected void scanBlockPosition(BlockPos position) {
        BlockState state = world.getBlockState(position);
        Block block = world.getBlockState(position).getBlock();
        if (state.canBeReplaced()) {
            BlockState stateDown = world.getBlockState(position.below());
            if (CropFarmRegistry.isTillable(stateDown)) {
                blocksToTill.add(position.below());
            } else if (CropFarmRegistry.isSoil(stateDown)) {
                blocksToPlant.add(position);
            }
        }

        if (state.getBlock() == Blocks.AIR) {
            return;
        }

        ICrop crop = CropFarmRegistry.getCrop(state);
        blocksToHarvest.addAll(crop.getPositionsToHarvest(world, position, state));

        if (crop.canBeFertilized(state, world, position)) {
            blocksToFertilize.add(position);
        }
    }

    @Override
    public WorkType getWorkType() {
        return WorkType.FARMING;
    }

    @Override
    public boolean onBlockClicked(Player player, @Nullable InteractionHand hand) {
        if (!player.level().isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_WORKSITE_CROP_FARM, pos);
        }
        return true;
    }

    private boolean hasToPlant() {
        return (plantableCount > 0 && !blocksToPlant.isEmpty());
    }

    private boolean hasToFertilize() {
        return (bonemealCount > 0 && !blocksToFertilize.isEmpty());
    }

    private static final IWorksiteAction PLANT_ACTION = e -> WorksiteImplementation.getEnergyPerActivation(e) / 5D;
    private static final IWorksiteAction FERTILIZE_ACTION = e -> WorksiteImplementation.getEnergyPerActivation(e) / 5D;
    private static final IWorksiteAction TILL_ACTION = e -> WorksiteImplementation.getEnergyPerActivation(e) / 5D;
    private static final IWorksiteAction HARVEST_ACTION = e -> WorksiteImplementation.getEnergyPerActivation(e) / 5D;

    @Override
    protected Optional<IWorksiteAction> getNextAction() {
        if (!blocksToHarvest.isEmpty()) {
            return Optional.of(HARVEST_ACTION);
        } else if (hasToFertilize()) {
            return Optional.of(FERTILIZE_ACTION);
        } else if (hasToPlant()) {
            return Optional.of(PLANT_ACTION);
        } else if (!blocksToTill.isEmpty()) {
            return Optional.of(TILL_ACTION);
        }

        return Optional.empty();
    }

    @Override
    protected boolean processAction(IWorksiteAction action) {
        if (action == TILL_ACTION) {
            return tryTill();
        } else if (action == HARVEST_ACTION) {
            return tryHarvest();
        } else if (action == PLANT_ACTION) {
            return tryPlant();
        } else if (action == FERTILIZE_ACTION) {
            return tryFertilize();
        }
        return false;
    }

    private boolean tryFertilize() {
        Iterator<BlockPos> it = blocksToFertilize.iterator();
        BlockPos position;
        while (it.hasNext() && (position = it.next()) != null) {
            it.remove();
            BlockState state = world.getBlockState(position);
            Block block = state.getBlock();
            if (block instanceof BonemealableBlock) {
                for (int slot = 0; slot < miscInventory.getSlots(); slot++) {
                    ItemStack stack = miscInventory.getStackInSlot(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (isBonemeal(stack)) {
                        ItemStack clone = stack.copy();
                        if (BoneMealItem.growCrop(clone, world, position)) {
                            miscInventory.extractItem(slot, 1, false);
                            world.levelEvent(2005, position, 0);
                        }
                        BlockState updatedState = world.getBlockState(position);
                        block = updatedState.getBlock();
                        if (block instanceof BonemealableBlock growable) {
                            if (growable.isValidBonemealTarget(world, position, updatedState, world.isClientSide)) {
                                blocksToFertilize.add(position);
                            } else if (isFarmable(block, position)) {
                                blocksToHarvest.add(position);
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
        }
        return false;
    }

    private boolean tryPlant() {
        Iterator<BlockPos> it = blocksToPlant.iterator();
        BlockPos position;
        while (it.hasNext() && (position = it.next()) != null) {
            it.remove();
            if (canReplace(position)) {
                for (int slot = 0; slot < plantableInventory.getSlots(); slot++) {
                    ItemStack stack = plantableInventory.getStackInSlot(slot);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    if (isPlantable(stack)) {
                        ItemStack clone = stack.copy();
                        if (tryPlace(clone, position, Direction.UP)) {
                            plantableInventory.extractItem(slot, 1, false);
                            return true;
                        }
                    }
                }
                return false;
            }
        }
        return false;
    }

    private boolean tryHarvest() {
        Iterator<BlockPos> it = blocksToHarvest.iterator();
        BlockPos position;
        if (it.hasNext() && (position = it.next()) != null) {
            it.remove();
            BlockState state = world.getBlockState(position);
            ICrop crop = CropFarmRegistry.getCrop(state);
            return crop.harvest(world, state, position, getFortune(), inventoryForDrops);
        }
        return false;
    }

    private boolean tryTill() {
        Iterator<BlockPos> it = blocksToTill.iterator();
        BlockPos position;
        while (it.hasNext() && (position = it.next()) != null) {
            it.remove();
            BlockState state = world.getBlockState(position);
            if (CropFarmRegistry.isTillable(state) && canReplace(position.above())) {
                world.setBlock(position, CropFarmRegistry.getTilledState(state), 3);
                world.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                return true;
            }
        }
        return false;
    }
}
