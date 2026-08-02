package net.shadowmage.ancientwarfare.automation.tile.worksite.cropfarm;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;

import java.util.List;

public interface ICrop {
    List<BlockPos> getPositionsToHarvest(Level world, BlockPos pos, BlockState state);

    boolean canBeFertilized(BlockState state, Level world, BlockPos pos);

    boolean harvest(Level world, BlockState state, BlockPos pos, int fortune, IItemHandler inventory);

    boolean matches(BlockState state);

    default boolean matches(ItemStack stack) {
        return false;
    }

    default boolean isPlantable(ItemStack stack) {
        return false;
    }
}
