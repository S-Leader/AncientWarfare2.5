package net.shadowmage.ancientwarfare.automation.tile.worksite.cropfarm;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;

import java.util.ArrayList;
import java.util.List;

public class CropStem implements ICrop {
    @Override
    public List<BlockPos> getPositionsToHarvest(Level world, BlockPos pos, BlockState state) {
        List<BlockPos> ret = new ArrayList<>();

        addPositionIfGourd(ret, world, pos.north());
        addPositionIfGourd(ret, world, pos.west());
        addPositionIfGourd(ret, world, pos.south());
        addPositionIfGourd(ret, world, pos.east());
        return ret;
    }

    @Override
    public boolean canBeFertilized(BlockState state, Level world, BlockPos pos) {
        return state.getBlock() instanceof BonemealableBlock growable && growable.isValidBonemealTarget(world, pos, state, world.isClientSide);
    }

    private void addPositionIfGourd(List<BlockPos> list, Level world, BlockPos pos) {
        if (LegacyMaterial.of(world.getBlockState(pos)) == LegacyMaterial.GOURD) {
            list.add(pos);
        }
    }

    @Override
    public boolean harvest(Level world, BlockState state, BlockPos pos, int fortune, IItemHandler inventory) {
        return false;
    }

    @Override
    public boolean matches(BlockState state) {
        return state.getBlock() instanceof StemBlock;
    }
}
