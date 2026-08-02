package net.shadowmage.ancientwarfare.structure.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.block.BlockBase;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.util.RayTraceUtils;
import net.shadowmage.ancientwarfare.structure.util.RotationLimit;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;
import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.VISIBLE;

public class BlockTribalChair extends BlockSeat {
    public BlockTribalChair() {
        super(LegacyMaterial.WOOD, "tribal_chair");
    }

    @Override
    public RotationLimit getRotationLimit(Level world, BlockPos seatPos, BlockState state) {
        return new RotationLimit.FacingQuarter(state.getValue(FACING));
    }

    @Override
    public void breakBlock(Level world, BlockPos pos, BlockState state) {
        BlockPos otherPos = state.getValue(VISIBLE) ? pos.above() : pos.below();
        if (world.getBlockState(otherPos).getBlock() == this) {
            world.removeBlock(otherPos, false);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        world.setBlock(pos, state.setValue(FACING, placer.getDirection().getOpposite()).setValue(VISIBLE, true), 3);
        world.setBlock(pos.above(), state.setValue(FACING, placer.getDirection().getOpposite()).setValue(VISIBLE, false), 3);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VISIBLE);
    }

    @Override
    protected Vec3 getSeatOffset() {
        return new Vec3(0.5, 0.35, 0.5);
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

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player playerIn, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (state.getValue(VISIBLE)) {
            return super.onBlockActivated(world, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
        }
        BlockState stateDown = world.getBlockState(pos.below());
        return stateDown.getBlock() instanceof BlockBase blockBase
                && blockBase.onBlockActivated(world, pos.below(), stateDown, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    private static final Map<Direction, List<AABB>> BOTTOM_AABBs = ImmutableMap.of(
            Direction.NORTH, new ImmutableList.Builder<AABB>()
                    .add(new AABB(0, 0, 1 / 16D, 1, 9 / 16D, 15 / 16D))
                    .add(new AABB(0, 9 / 16D, 13 / 16D, 1, 1, 15 / 16D))
                    .add(new AABB(0, 9 / 16D, 1 / 16D, 2 / 16D, 14 / 16D, 13 / 16D))
                    .add(new AABB(14 / 16D, 9 / 16D, 1 / 16D, 1, 14 / 16D, 13 / 16D))
                    .build(),
            Direction.SOUTH, new ImmutableList.Builder<AABB>()
                    .add(new AABB(0, 0, 1 / 16D, 1, 9 / 16D, 15 / 16D))
                    .add(new AABB(0, 9 / 16D, 1 / 16D, 1, 1, 3 / 16D))
                    .add(new AABB(0, 9 / 16D, 3 / 16D, 2 / 16D, 14 / 16D, 15 / 16D))
                    .add(new AABB(14 / 16D, 9 / 16D, 3 / 16D, 1, 14 / 16D, 15 / 16D))
                    .build(),
            Direction.EAST, new ImmutableList.Builder<AABB>()
                    .add(new AABB(1 / 16D, 0, 0, 15 / 16D, 9 / 16D, 1))
                    .add(new AABB(1 / 16D, 9 / 16D, 0, 3 / 16D, 1, 1))
                    .add(new AABB(3 / 16D, 9 / 16D, 0, 15 / 16D, 14 / 16D, 2 / 16D))
                    .add(new AABB(3 / 16D, 9 / 16D, 14 / 16D, 15 / 16D, 14 / 16D, 1))
                    .build(),
            Direction.WEST, new ImmutableList.Builder<AABB>()
                    .add(new AABB(1 / 16D, 0, 0, 15 / 16D, 9 / 16D, 1))
                    .add(new AABB(13 / 16D, 9 / 16D, 0, 15 / 16D, 1, 1))
                    .add(new AABB(1 / 16D, 9 / 16D, 0, 13 / 16D, 14 / 16D, 2 / 16D))
                    .add(new AABB(1 / 16D, 9 / 16D, 14 / 16D, 13 / 16D, 14 / 16D, 1))
                    .build()
    );

    private static final Map<Direction, AABB> TOP_AABBs = ImmutableMap.of(
            Direction.NORTH, new AABB(0, 0, 13 / 16D, 1, 9 / 16D, 15 / 16D),
            Direction.SOUTH, new AABB(0, 0, 1 / 16D, 1, 9 / 16D, 3 / 16D),
            Direction.EAST, new AABB(1 / 16D, 0, 0, 3 / 16D, 9 / 16D, 1),
            Direction.WEST, new AABB(13 / 16D, 0, 0, 15 / 16D, 9 / 16D, 1)
    );

    @Nullable
    public HitResult collisionRayTrace(BlockState blockState, Level world, BlockPos pos, Vec3 start, Vec3 end) {
        Direction facing = blockState.getValue(FACING);

        return blockState.getValue(VISIBLE) ? RayTraceUtils.raytraceMultiAABB(BOTTOM_AABBs.get(facing), pos, start, end, (rtr, aabb) -> rtr) :
                RayTraceUtils.raytraceMultiAABB(ImmutableList.of(TOP_AABBs.get(facing)), pos, start, end);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        if (state.getValue(VISIBLE)) {
            VoxelShape shape = Shapes.empty();
            for (AABB aabb : BOTTOM_AABBs.get(state.getValue(FACING))) {
                shape = Shapes.or(shape, Shapes.create(aabb));
            }
            return shape;
        } else {
            return Shapes.create(TOP_AABBs.get(state.getValue(FACING)));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getSelectedBoundingBox(BlockState state, Level world, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        if (!state.getValue(VISIBLE)) {
            return TOP_AABBs.get(facing).move(pos);
        }
        LocalPlayer player = Minecraft.getInstance().player;
        return RayTraceUtils.getSelectedBoundingBox(BOTTOM_AABBs.get(facing), pos, player);
    }
}
