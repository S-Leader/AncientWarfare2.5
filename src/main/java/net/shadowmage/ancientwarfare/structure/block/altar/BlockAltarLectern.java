package net.shadowmage.ancientwarfare.structure.block.altar;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class BlockAltarLectern extends BlockAltarTop {
    private static final AABB AABB_NORTH = new AABB(0, 0, 0, 1D, 9 / 16D, 14 / 16D);
    private static final AABB AABB_SOUTH = new AABB(0, 0, 2 / 16D, 1D, 9 / 16D, 1D);
    private static final AABB AABB_WEST = new AABB(0, 0, 0, 14 / 16D, 9 / 16D, 1D);
    private static final AABB AABB_EAST = new AABB(2 / 16D, 0, 0, 1D, 9 / 16D, 1D);

    public BlockAltarLectern() {
        super(LegacyMaterial.WOOD, "altar_lectern");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(FACING, Direction.from2DDataValue(meta));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(FACING).get2DDataValue();
    }

    @Override
    public BlockState getStateForPlacement(Level worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        return defaultBlockState().setValue(FACING, placer.getDirection().getOpposite());
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case NORTH:
                return AABB_NORTH;
            case SOUTH:
                return AABB_SOUTH;
            case WEST:
                return AABB_WEST;
            case EAST:
                return AABB_EAST;
        }
        return new AABB(2 / 16D, 0, 0, 1D, 9 / 16D, 1D);
    }
}
