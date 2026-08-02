package net.shadowmage.ancientwarfare.automation.tile.worksite.fruitfarm;


import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.shadowmage.ancientwarfare.automation.registry.FruitFarmRegistry;
import net.shadowmage.ancientwarfare.automation.tile.worksite.IWorksiteAction;
import net.shadowmage.ancientwarfare.automation.tile.worksite.TileWorksiteFarm;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class WorkSiteFruitFarm extends TileWorksiteFarm {
    private static final int MAX_HEIGHT_SEARCH = 7;

    private Set<BlockPos> blocksToPick = new LinkedHashSet<>();
    private Set<BlockPos> blocksToPlant = new LinkedHashSet<>();
    private Set<BlockPos> blocksToBoneMeal = new LinkedHashSet<>();

    private final IItemHandler inventoryForDrops;

    public WorkSiteFruitFarm() {
        super();
        inventoryForDrops = new CombinedInvWrapper(plantableInventory, mainInventory);
    }

    @Override
    public void onBoundsAdjusted() {
        super.onBoundsAdjusted();

        validateCollection(blocksToPick);
        validateCollection(blocksToPlant);
    }

    @Override
    public boolean onBlockClicked(Player player, @Nullable InteractionHand hand) {
        if (!player.level().isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_WORKSITE_FRUIT_FARM, pos);
        }
        return true;
    }

    @Override
    public WorkType getWorkType() {
        return WorkType.FARMING;
    }

    @Override
    protected boolean isPlantable(ItemStack stack) {
        return FruitFarmRegistry.isPlantable(stack);
    }

    @Override
    protected void scanBlockPosition(BlockPos pos) {
        for (BlockPos currentPos = pos; currentPos.getY() < pos.getY() + MAX_HEIGHT_SEARCH; currentPos = currentPos.above()) {
            BlockState state = world.getBlockState(currentPos);
            if (FruitFarmRegistry.getPickable(state).isRipe(state)) {
                blocksToPick.add(currentPos);
            }

            BlockPos curPos = currentPos; // because streams need effectively final vars
            InventoryTools.stream(plantableInventory).filter(s -> FruitFarmRegistry.getPlantable(s).canPlant(world, curPos, state))
                    .forEach(s -> blocksToPlant.add(curPos));

            if (canBoneMeal(world, currentPos, state)) {
                blocksToBoneMeal.add(currentPos);
            }
        }
    }

    private boolean canBoneMeal(Level world, BlockPos currentPos, BlockState state) {
        return state.getBlock() instanceof BonemealableBlock growable && growable.isValidBonemealTarget(world, currentPos, state, world.isClientSide);
    }

    private boolean boneMeal() {
        if (blocksToBoneMeal.isEmpty()) {
            return false;
        }

        Iterator<BlockPos> it = blocksToBoneMeal.iterator();
        BlockPos pos = it.next();
        it.remove();

        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof BonemealableBlock && fertilize(pos);
    }

    private boolean plantFruits() {
        if (blocksToPlant.isEmpty()) {
            return false;
        }

        Iterator<BlockPos> it = blocksToPlant.iterator();
        BlockPos plantPos = it.next();
        it.remove();
        Optional<IFruit> plantableFruit = InventoryTools.stream(plantableInventory).map(FruitFarmRegistry::getPlantable)
                .filter(p -> p.canPlant(world, plantPos, world.getBlockState(plantPos))).findFirst();

        return plantableFruit.isPresent() && plantableFruit.get().plant(world, plantPos);
    }

    private boolean pickFruits() {
        if (blocksToPick.isEmpty()) {
            return false;
        }

        Iterator<BlockPos> it = blocksToPick.iterator();
        BlockPos pickPos = it.next();
        it.remove();
        BlockState state = world.getBlockState(pickPos);
        IFruit pickable = FruitFarmRegistry.getPickable(state);

        return pickable.pick(world, state, pickPos, getFortune(), inventoryForDrops);
    }

    private static final IWorksiteAction PICK_ACTION = WorksiteImplementation::getEnergyPerActivation;
    private static final IWorksiteAction PLANT_ACTION = WorksiteImplementation::getEnergyPerActivation;
    private static final IWorksiteAction BONEMEAL_ACTION = WorksiteImplementation::getEnergyPerActivation;

    @Override
    protected Optional<IWorksiteAction> getNextAction() {
        if (!blocksToPick.isEmpty()) {
            return Optional.of(PICK_ACTION);
        } else if (!blocksToPlant.isEmpty()) {
            return Optional.of(PLANT_ACTION);
        } else if (!blocksToBoneMeal.isEmpty()) {
            return Optional.of(BONEMEAL_ACTION);
        }

        return Optional.empty();
    }

    @Override
    protected boolean processAction(IWorksiteAction action) {
        if (action == PICK_ACTION) {
            return pickFruits();
        } else if (action == PLANT_ACTION) {
            return plantFruits();
        } else if (action == BONEMEAL_ACTION) {
            return boneMeal();
        }
        return false;
    }
}
