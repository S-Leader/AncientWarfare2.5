package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.structure.util.RotationLimit;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class BlockTribalStool extends BlockSeat {
    private static final Vec3 SEAT_OFFSET = new Vec3(0.5, 0.4, 0.5);
    private static final AABB Z_AXIS_AABB = new AABB(0, 0, 1 / 16D, 1, 10 / 16D, 15 / 16D);
    private static final AABB X_AXIS_AABB = new AABB(1 / 16D, 0, 0, 15 / 16D, 10 / 16D, 1);

    public BlockTribalStool() {
        super(LegacyMaterial.WOOD, "tribal_stool");
    }

    @Override
    public RotationLimit getRotationLimit(Level world, BlockPos seatPos, BlockState state) {
        return RotationLimit.NO_LIMIT;
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
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        world.setBlock(pos, state.setValue(FACING, placer.getDirection().getOpposite()), 3);
    }

    @Override
    protected Vec3 getSeatOffset() {
        return SEAT_OFFSET;
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? Z_AXIS_AABB : X_AXIS_AABB;
    }
}
