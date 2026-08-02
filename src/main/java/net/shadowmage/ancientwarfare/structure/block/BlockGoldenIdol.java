package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;

import static net.minecraft.world.level.block.RenderShape.INVISIBLE;
import static net.minecraft.world.level.block.RenderShape.MODEL;
import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class BlockGoldenIdol extends BlockBaseStructure {
    private static final BooleanProperty BOTTOM_PART = BooleanProperty.create("bottom_part");

    public BlockGoldenIdol() {
        super(LegacyMaterial.IRON, "golden_idol");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BOTTOM_PART);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(FACING, Direction.from2DDataValue(meta)).setValue(BOTTOM_PART, ((meta >> 2) & 1) > 0);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(FACING).get2DDataValue() | (state.getValue(BOTTOM_PART) ? 1 : 0) << 2;
    }

    @Override
    public BlockState getStateForPlacement(Level worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        return defaultBlockState().setValue(FACING, placer.getDirection()).setValue(BOTTOM_PART, true);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(BOTTOM_PART) ? MODEL : INVISIBLE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return world.getBlockState(pos).canBeReplaced() && world.getBlockState(pos.above()).canBeReplaced();
    }

    @Override
    public void onBlockPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        worldIn.setBlock(pos.above(), state.setValue(BOTTOM_PART, false), 3);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean movedByPiston) {
        Boolean bottomPart = state.getValue(BOTTOM_PART);
        BlockPos otherPos = bottomPart ? pos.above() : pos.below();
        if (worldIn.getBlockState(otherPos).getBlock() != this) {
            worldIn.removeBlock(pos, false);
            if (bottomPart) {
                dropResources(state, worldIn, pos);
            }
        }
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

}
