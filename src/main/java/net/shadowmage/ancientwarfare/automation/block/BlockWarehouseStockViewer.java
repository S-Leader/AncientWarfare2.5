package net.shadowmage.ancientwarfare.automation.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.gui.GuiWarehouseStockViewer;
import net.shadowmage.ancientwarfare.automation.tile.warehouse2.TileWarehouseStockViewer;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableBlock;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RotationType;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import javax.annotation.Nullable;
import java.util.Map;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class BlockWarehouseStockViewer extends BlockBaseAutomation implements IRotatableBlock {
    private static final Map<Direction, AABB> AABBS;

    static {
        float wmin = 0.125f;
        float wmax = 0.875f;
        float hmin = 0.375f;
        float hmax = 0.875f;
        AABBS = ImmutableMap.of(Direction.WEST, new AABB(wmax, hmin, 0, 1.f, hmax, 1), Direction.EAST, new AABB(0, hmin, 0, wmin, hmax, 1), Direction.SOUTH, new AABB(0, hmin, 0, 1, hmax, wmin), Direction.NORTH, new AABB(0, hmin, wmax, 1, hmax, 1));
    }

    public BlockWarehouseStockViewer(String regName) {
        super(LegacyMaterial.ROCK, regName);
        setHardness(2.f);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(FACING, BlockTools.getHorizontalFacingFromMeta(meta));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(FACING).ordinal();
    }

    @Override
    public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        return defaultBlockState().setValue(FACING, placer.getDirection());
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldSideBeRendered(BlockState blockState, BlockGetter world, BlockPos pos, Direction side) {
        return true;
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
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean invertFacing() {
        return true;
    }

    @Nullable
    @Override
    public AABB getCollisionBoundingBox(BlockState blockState, BlockGetter worldIn, BlockPos pos) {
        return null;
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return AABBS.get(state.getValue(FACING));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        return WorldTools.clickInteractableTileWithHand(world, pos, player, hand);
    }

    @Override
    public boolean eventReceived(BlockState state, Level world, BlockPos pos, int id, int param) {
        super.eventReceived(state, world, pos, id, param);
        return WorldTools.sendClientEventToTile(world, pos, id, param);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();
    }
}
