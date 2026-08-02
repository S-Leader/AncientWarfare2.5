package net.shadowmage.ancientwarfare.structure.block.altar;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.structure.block.BlockBaseStructure;

public abstract class BlockAltarTop extends BlockBaseStructure {
    public BlockAltarTop(LegacyMaterial material, String regName) {
        super(material, regName);
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(BlockState state) {
        return false;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return canSupportCenter(world, pos.below(), Direction.UP);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean movedByPiston) {
        if (!fromPos.equals(pos.below())) {
            return;
        }
        if (!canSurvive(state, world, pos)) {
            dropResources(world.getBlockState(pos), world, pos, world.getBlockEntity(pos));
            world.removeBlock(pos, false);
        }
    }
}
