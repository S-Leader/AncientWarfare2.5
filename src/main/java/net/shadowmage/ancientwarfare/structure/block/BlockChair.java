package net.shadowmage.ancientwarfare.structure.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.block.BlockBase;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.util.RayTraceUtils;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.tile.TileChair;
import net.shadowmage.ancientwarfare.structure.util.RotationLimit;
import net.shadowmage.ancientwarfare.structure.util.WoodVariantHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;
import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.VISIBLE;
import static net.shadowmage.ancientwarfare.structure.util.BlockStateProperties.VARIANT;

public class BlockChair extends BlockSeat {
    private static final Vec3 SEAT_OFFSET = new Vec3(0.5, 0.47, 0.5);

    public BlockChair() {
        super(LegacyMaterial.WOOD, "chair");
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos) {
        return state.setValue(FACING, WorldTools.getTile(world, pos, TileChair.class).map(TileChair::getPrimaryFacing).orElse(Direction.NORTH));
    }

    public void getSubBlocks(CreativeModeTab itemIn, NonNullList<ItemStack> items) {
        WoodVariantHelper.getSubBlocks(this, items);
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }
        world.setBlock(pos, state.setValue(VARIANT, WoodVariantHelper.getVariant(stack)).setValue(VISIBLE, true), 3);
        WorldTools.getTile(world, pos, TileChair.class).ifPresent(te -> te.setPrimaryFacing(placer.getDirection().getOpposite()));
        world.setBlock(pos.above(), defaultBlockState().setValue(VARIANT, WoodVariantHelper.getVariant(stack)).setValue(VISIBLE, false), 3);
        WorldTools.getTile(world, pos.above(), TileChair.class).ifPresent(te -> {
            te.setPrimaryFacing(placer.getDirection().getOpposite());
            te.setMainBlockPos(pos);
        });
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, Level world, BlockPos pos, Player player) {
        return WoodVariantHelper.getPickBlock(this, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT, FACING, VISIBLE);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(VARIANT, WoodVariant.byMeta(meta & 7)).setValue(VISIBLE, ((meta >> 3) & 1) > 0);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(VARIANT).getMeta() | (state.getValue(VISIBLE) ? 1 : 0) << 3;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity createTileEntity(Level world, BlockState state) {
        return new TileChair();
    }

    private static final List<AABB> AABBs = ImmutableList.of(
            new AABB(1 / 16D, 9 / 16D, 1 / 16D, 15 / 16D, 11 / 16D, 15 / 16D),
            new AABB(1 / 16D, 0, 1 / 16D, 3 / 16D, 9 / 16D, 3 / 16D),
            new AABB(13 / 16D, 0, 1 / 16D, 15 / 16D, 9 / 16D, 3 / 16D),
            new AABB(13 / 16D, 0, 13 / 16D, 15 / 16D, 9 / 16D, 15 / 16D),
            new AABB(1 / 16D, 0, 13 / 16D, 3 / 16D, 9 / 16D, 15 / 16D));

    private static final Map<Direction, List<AABB>> BOTTOM_AABBs = ImmutableMap.of(
            Direction.NORTH, new ImmutableList.Builder<AABB>().addAll(AABBs)
                    .add(new AABB(1 / 16D, 11 / 16D, 13 / 16D, 15 / 16D, 1, 15 / 16D)).build(),
            Direction.SOUTH, new ImmutableList.Builder<AABB>().addAll(AABBs)
                    .add(new AABB(1 / 16D, 11 / 16D, 1 / 16D, 15 / 16D, 1, 3 / 16D)).build(),
            Direction.EAST, new ImmutableList.Builder<AABB>().addAll(AABBs)
                    .add(new AABB(1 / 16D, 11 / 16D, 1 / 16D, 3 / 16D, 1, 15 / 16D)).build(),
            Direction.WEST, new ImmutableList.Builder<AABB>().addAll(AABBs)
                    .add(new AABB(13 / 16D, 11 / 16D, 1 / 16D, 15 / 16D, 1, 15 / 16D)).build()
    );

    private static final Map<Direction, AABB> TOP_AABBs = ImmutableMap.of(
            Direction.NORTH, new AABB(1 / 16D, 0, 13 / 16D, 15 / 16D, 9 / 16D, 15 / 16D),
            Direction.SOUTH, new AABB(1 / 16D, 0, 1 / 16D, 15 / 16D, 9 / 16D, 3 / 16D),
            Direction.EAST, new AABB(1 / 16D, 0, 1 / 16D, 3 / 16D, 9 / 16D, 15 / 16D),
            Direction.WEST, new AABB(13 / 16D, 0, 1 / 16D, 15 / 16D, 9 / 16D, 15 / 16D)
    );

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

    @Nullable
    public HitResult collisionRayTrace(BlockState blockState, Level world, BlockPos pos, Vec3 start, Vec3 end) {
        Direction facing = WorldTools.getTile(world, pos, TileChair.class).map(TileChair::getPrimaryFacing).orElse(Direction.NORTH);
        return blockState.getValue(VISIBLE) ? RayTraceUtils.raytraceMultiAABB(BOTTOM_AABBs.get(facing), pos, start, end, (rtr, aabb) -> rtr) :
                RayTraceUtils.raytraceMultiAABB(ImmutableList.of(TOP_AABBs.get(facing)), pos, start, end);
    }

    @Nullable
    @Override
    public AABB getCollisionBoundingBox(BlockState blockState, BlockGetter world, BlockPos pos) {
        return blockState.getValue(VISIBLE) ? AABBs.get(0) : TOP_AABBs.get(WorldTools.getTile(world, pos, TileChair.class).map(TileChair::getPrimaryFacing).orElse(Direction.NORTH));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getSelectedBoundingBox(BlockState state, Level world, BlockPos pos) {
        Direction facing = WorldTools.getTile(world, pos, TileChair.class).map(TileChair::getPrimaryFacing).orElse(Direction.NORTH);
        if (!state.getValue(VISIBLE)) {
            return TOP_AABBs.get(facing).move(pos);
        }
        LocalPlayer player = Minecraft.getInstance().player;
        return RayTraceUtils.getSelectedBoundingBox(BOTTOM_AABBs.get(facing), pos, player);
    }

    @Override
    protected Vec3 getSeatOffset() {
        return SEAT_OFFSET;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        WoodVariantHelper.registerClient(this, propString -> "facing=north," + propString + ",visible=true");
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter world, BlockPos pos, BlockState state, int fortune) {
        WoodVariantHelper.getDrops(this, drops, state);
    }

    @Override
    public RotationLimit getRotationLimit(Level world, BlockPos seatPos, BlockState state) {
        return new RotationLimit.FacingThreeQuarters(WorldTools.getTile(world, seatPos, TileChair.class).map(TileChair::getPrimaryFacing).orElse(Direction.NORTH));
    }
}
