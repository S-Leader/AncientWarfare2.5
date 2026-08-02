package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.structure.util.RotationLimit;
import net.shadowmage.ancientwarfare.structure.util.WoodVariantHelper;

import static net.shadowmage.ancientwarfare.structure.render.property.StructureProperties.AXIS;
import static net.shadowmage.ancientwarfare.structure.util.BlockStateProperties.VARIANT;

public class BlockBench extends BlockSeat {
    private static final EnumProperty<Legs> LEGS = EnumProperty.create("legs", Legs.class);
    private static final Vec3 SEAT_OFFSET = new Vec3(0.5, 0.35, 0.5);

    public BlockBench() {
        super(LegacyMaterial.WOOD, "bench");
    }

    public void getSubBlocks(CreativeModeTab itemIn, NonNullList<ItemStack> items) {
        WoodVariantHelper.getSubBlocks(this, items);
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }
        Direction.Axis perpendicularAxis = placer.getDirection().getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        world.setBlock(pos, state.setValue(VARIANT, WoodVariantHelper.getVariant(stack)).setValue(AXIS, perpendicularAxis), 3);
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, Level world, BlockPos pos, Player player) {
        return WoodVariantHelper.getPickBlock(this, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT, AXIS, LEGS);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(VARIANT, WoodVariant.byMeta(meta & 7)).setValue(AXIS, ((meta >> 3) & 1) > 0 ? Direction.Axis.Z : Direction.Axis.X);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(VARIANT).getMeta() | (state.getValue(AXIS) == Direction.Axis.Z ? 1 : 0) << 3;
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos) {
        Direction.Axis axis = state.getValue(AXIS);

        Legs legs = Legs.NONE;
        if (axis == Direction.Axis.X) {
            boolean west = isSameAxisBench(state, world, pos.west());
            boolean east = isSameAxisBench(state, world, pos.east());

            if (west && !east) {
                legs = Legs.LEFT;
            } else if (!west && east) {
                legs = Legs.RIGHT;
            } else if (!west) {
                legs = Legs.BOTH;
            }
        } else {
            boolean south = isSameAxisBench(state, world, pos.south());
            boolean north = isSameAxisBench(state, world, pos.north());

            if (north && !south) {
                legs = Legs.RIGHT;
            } else if (!north && south) {
                legs = Legs.LEFT;
            } else if (!north) {
                legs = Legs.BOTH;
            }
        }
        return state.setValue(LEGS, legs);
    }

    private boolean isSameAxisBench(BlockState thisState, BlockGetter world, BlockPos neighborPos) {
        BlockState neighborState = world.getBlockState(neighborPos);
        return neighborState.getBlock() == this && thisState.getValue(AXIS) == neighborState.getValue(AXIS);
    }

    @Override
    public RotationLimit getRotationLimit(Level world, BlockPos seatPos, BlockState state) {
        return RotationLimit.NO_LIMIT;
    }

    @Override
    protected Vec3 getSeatOffset() {
        return SEAT_OFFSET;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter world, BlockPos pos, BlockState state, int fortune) {
        WoodVariantHelper.getDrops(this, drops, state);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        WoodVariantHelper.registerClient(this, propString -> "axis=x,legs=both," + propString);
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return state.getValue(AXIS) == Direction.Axis.X ? new AABB(0, 0, 3 / 16d, 1, 9 / 16d, 13 / 16d) :
                new AABB(3 / 16d, 0, 0, 13 / 16d, 9 / 16d, 1);
    }

    public enum Legs implements StringRepresentable {
        NONE("none"),
        LEFT("left"),
        RIGHT("right"),
        BOTH("both");

        private String name;

        Legs(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
