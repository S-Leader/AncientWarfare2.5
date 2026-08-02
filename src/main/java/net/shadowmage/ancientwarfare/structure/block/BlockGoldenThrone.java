package net.shadowmage.ancientwarfare.structure.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.block.BlockBase;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;
import net.shadowmage.ancientwarfare.core.render.model.LegacyStateMapperBase;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;
import net.shadowmage.ancientwarfare.structure.render.property.TopBottomPart;
import net.shadowmage.ancientwarfare.structure.util.RotationLimit;

import java.util.Map;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;
import static net.shadowmage.ancientwarfare.structure.render.property.StructureProperties.TOP_BOTTOM_PART;

public class BlockGoldenThrone extends BlockSeat {
    public BlockGoldenThrone() {
        super(LegacyMaterial.IRON, "golden_throne");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TOP_BOTTOM_PART);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(FACING, Direction.from2DDataValue(meta & 3)).setValue(TOP_BOTTOM_PART, TopBottomPart.byMeta((meta >> 2) & 1));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(FACING).get2DDataValue() | state.getValue(TOP_BOTTOM_PART).getMeta() << 2;
    }

    private static final Map<Direction, AABB> TOP_AABBs = ImmutableMap.of(
            Direction.NORTH, new AABB(0, 0, 13 / 16D, 1, 1, 15 / 16D),
            Direction.SOUTH, new AABB(0, 0, 1 / 16D, 1, 1, 3 / 16D),
            Direction.EAST, new AABB(1 / 16D, 0, 0, 3 / 16D, 1, 1),
            Direction.WEST, new AABB(13 / 16D, 0, 0, 15 / 16D, 1, 1)
    );

    private static final Map<Direction, AABB> BOTTOM_AABBs = ImmutableMap.of(
            Direction.NORTH, new AABB(0, 0, 1 / 16D, 1, 1, 15 / 16D),
            Direction.SOUTH, new AABB(0, 0, 1 / 16D, 1, 1, 15 / 16D),
            Direction.EAST, new AABB(1 / 16D, 0, 0, 15 / 16D, 1, 1),
            Direction.WEST, new AABB(1 / 16D, 0, 0, 15 / 16D, 1, 1)
    );

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return state.getValue(TOP_BOTTOM_PART) == TopBottomPart.BOTTOM ? BOTTOM_AABBs.get(state.getValue(FACING)) : TOP_AABBs.get(state.getValue(FACING));
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        world.setBlock(pos.above(), state.setValue(FACING, placer.getDirection().getOpposite()).setValue(TOP_BOTTOM_PART, TopBottomPart.TOP), 3);
        world.setBlock(pos, state.setValue(FACING, placer.getDirection().getOpposite()).setValue(TOP_BOTTOM_PART, TopBottomPart.BOTTOM), 3);
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player playerIn, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (state.getValue(TOP_BOTTOM_PART) == TopBottomPart.BOTTOM) {
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
        BlockPos otherPos = state.getValue(TOP_BOTTOM_PART) == TopBottomPart.BOTTOM ? pos.above() : pos.below();
        if (!world.isEmptyBlock(otherPos)) {
            world.removeBlock(otherPos, false);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        final ResourceLocation assetLocation = new ResourceLocation(AncientWarfareCore.MOD_ID, "structure/" + getRegistryName().getPath());
        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return new ModelResourceLocation(assetLocation, getPropertyString(state.getValues()));
            }
        });

        ModelLoaderHelper.registerItem(this, "structure", "inventory");
    }
}
