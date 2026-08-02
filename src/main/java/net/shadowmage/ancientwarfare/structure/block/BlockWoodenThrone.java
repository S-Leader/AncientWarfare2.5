package net.shadowmage.ancientwarfare.structure.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.core.block.BlockBase;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.structure.util.RotationLimit;

import java.util.Map;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;
import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.VISIBLE;

public class BlockWoodenThrone extends BlockSeat {
    public BlockWoodenThrone() {
        super(LegacyMaterial.WOOD, "wooden_throne");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VISIBLE);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(FACING, Direction.from2DDataValue(meta & 3)).setValue(VISIBLE, ((meta >> 2) & 1) > 0);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(FACING).get2DDataValue() | (state.getValue(VISIBLE) ? 1 : 0) << 2;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return state.getValue(VISIBLE) ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    private static final Map<Direction, AABB> TOP_AABBs = ImmutableMap.of(
            Direction.NORTH, new AABB(0, 0, 13 / 16D, 1, 1, 1),
            Direction.SOUTH, new AABB(0, 0, 0, 1, 1, 3 / 16D),
            Direction.EAST, new AABB(0, 0, 0, 3 / 16D, 1, 1),
            Direction.WEST, new AABB(13 / 16D, 0, 0, 1, 1, 1)
    );

    private static final Map<Direction, AABB> BOTTOM_AABBs = ImmutableMap.of(
            Direction.NORTH, new AABB(0, 0, 1 / 16D, 1, 1, 1),
            Direction.SOUTH, new AABB(0, 0, 0, 1, 1, 15 / 16D),
            Direction.EAST, new AABB(0, 0, 0, 15 / 16D, 1, 1),
            Direction.WEST, new AABB(1 / 16D, 0, 0, 1, 1, 1)
    );

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return state.getValue(VISIBLE) ? BOTTOM_AABBs.get(state.getValue(FACING)) : TOP_AABBs.get(state.getValue(FACING));
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        world.setBlock(pos.above(), state.setValue(FACING, placer.getDirection().getOpposite()).setValue(VISIBLE, false), 3);
        world.setBlock(pos, state.setValue(FACING, placer.getDirection().getOpposite()).setValue(VISIBLE, true), 3);
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player playerIn, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (state.getValue(VISIBLE)) {
            return super.onBlockActivated(world, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
        }
        BlockState stateDown = world.getBlockState(pos.below());
        return stateDown.getBlock() instanceof BlockBase blockBase
                && blockBase.onBlockActivated(world, pos.below(), stateDown, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public RotationLimit getRotationLimit(Level world, BlockPos seatPos, BlockState state) {
        return new RotationLimit.FacingQuarter(state.getValue(FACING));
    }

    @Override
    public void breakBlock(Level world, BlockPos pos, BlockState state) {
        BlockPos otherPos = state.getValue(VISIBLE) ? pos.above() : pos.below();
        if (!world.isEmptyBlock(otherPos)) {
            world.removeBlock(otherPos, false);
        }
        super.breakBlock(world, pos, state);
    }
}
