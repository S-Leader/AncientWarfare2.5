package net.shadowmage.ancientwarfare.automation.tile.worksite.cropfarm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import java.util.Collections;
import java.util.List;

public class CropDefault implements ICrop {
    @Override
    public List<BlockPos> getPositionsToHarvest(Level world, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BonemealableBlock growable && !growable.isValidBonemealTarget(world, pos, state, world.isClientSide)) {
            return Collections.singletonList(pos);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean canBeFertilized(BlockState state, Level world, BlockPos pos) {
        if (!(state.getBlock() instanceof BonemealableBlock growable)) {
            return false;
        }

        return growable.isValidBonemealTarget(world, pos, state, world.isClientSide)
                && growable.isBonemealSuccess(world, world.getRandom(), pos, state);
    }

    @Override
    public boolean harvest(Level world, BlockState state, BlockPos pos, int fortune, IItemHandler inventory) {
        NonNullList<ItemStack> stacks = NonNullList.create();

        getDrops(stacks, world, pos, state, fortune);

        if (!InventoryTools.canInventoryHold(inventory, stacks)) {
            return false;
        }

        if (!breakCrop(world, pos, state)) {
            return false;
        }

        ItemStack plantable = InventoryTools.removeItem(stacks, i -> i.getItem() instanceof IPlantable, 1);

        if (!plantable.isEmpty()) {
            BlockTools.placeItemBlock(plantable, world, pos, Direction.UP);
        }

        InventoryTools.insertOrDropItems(inventory, stacks, world, pos);
        return true;
    }

    protected boolean breakCrop(Level world, BlockPos pos, BlockState state) {
        return BlockTools.breakBlockNoDrops(world, pos, state);
    }

    protected void getDrops(NonNullList<ItemStack> stacks, Level world, BlockPos pos, BlockState state, int fortune) {
        stacks.addAll(BlockTools.getDrops(world, pos, state, fortune));
    }

    @Override
    public boolean matches(BlockState state) {
        return true;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isPlantable(ItemStack stack) {
        return stack.getItem() instanceof IPlantable || isPlantableItemBlock(stack);
    }

    private boolean isPlantableItemBlock(ItemStack stack) {
        return stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof IPlantable;
    }
}
