package net.shadowmage.ancientwarfare.automation.tile.worksite.fruitfarm;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.parsing.BlockStateMatcher;
import net.shadowmage.ancientwarfare.core.util.parsing.ItemStackMatcher;

import java.util.Optional;

public class FruitCocoa implements IFruit {
    private BlockStateMatcher stateMatcher;
    private ItemStackMatcher stackMatcher;

    public FruitCocoa() {
        this.stateMatcher = new BlockStateMatcher(Blocks.COCOA);
        this.stackMatcher = new ItemStackMatcher.Builder(Items.COCOA_BEANS).build();
    }

    @Override
    public boolean matches(BlockState state) {
        return stateMatcher.test(state);
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stackMatcher.test(stack);
    }

    @Override
    public boolean canPlant(Level world, BlockPos pos, BlockState state) {
        return world.isEmptyBlock(pos) && Direction.Plane.HORIZONTAL.stream().anyMatch(h -> isJungleLog(world.getBlockState(pos.relative(h))));
    }

    private boolean isJungleLog(BlockState state) {
        return state.is(BlockTags.JUNGLE_LOGS);
    }

    @Override
    public boolean plant(Level world, BlockPos plantPos) {
        if (!world.isEmptyBlock(plantPos)) {
            return false;
        }

        Optional<Direction> facing = Direction.Plane.HORIZONTAL.stream().filter(h -> isJungleLog(world.getBlockState(plantPos.relative(h)))).findFirst();

        return facing.isPresent() && plantBean(world, plantPos, facing.get());
    }

    private boolean plantBean(Level world, BlockPos pos, Direction facing) {
        return world.setBlock(pos, Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.FACING, facing).setValue(CocoaBlock.AGE, 0), 3);
    }

    @Override
    public boolean isRipe(BlockState state) {
        return state.getValue(CocoaBlock.AGE) == 2;
    }

    @Override
    public boolean pick(Level world, BlockState state, BlockPos pos, int fortune, IItemHandler inventory) {
        NonNullList<ItemStack> drops = net.shadowmage.ancientwarfare.core.util.BlockTools.getDrops(world, pos, state, fortune);

        if (!InventoryTools.canInventoryHold(inventory, drops)) {
            return false;
        }

        BlockState newState = state.setValue(CocoaBlock.AGE, 0);

        world.setBlock(pos, newState, 3);
        world.levelEvent(2001, pos, Block.getId(newState));

        //remove that one cocoa bean that was just "replanted"
        InventoryTools.removeItem(drops, s -> s.is(Items.COCOA_BEANS), 1);

        InventoryTools.insertOrDropItems(inventory, drops, world, pos);

        return true;
    }

    @Override
    public boolean isPlantable() {
        return true;
    }
}
