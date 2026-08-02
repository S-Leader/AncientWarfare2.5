package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueBase;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableBlock;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableTile;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.render.BlockStateKeyGenerator;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelBakery;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelState;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

public abstract class BlockTorqueBase extends BlockBaseAutomation implements IRotatableBlock {

    protected BlockTorqueBase(LegacyMaterial material, String regName) {
        super(material, regName);
        setHardness(2.f);
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        return WorldTools.clickInteractableTileWithHand(world, pos, player, hand);
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
    public boolean isSideSolid(BlockState base_state, BlockGetter world, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean shouldSideBeRendered(BlockState blockState, BlockGetter world, BlockPos pos, Direction side) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        addProperties(builder);
        builder.add(CoreProperties.UNLISTED_FACING);
    }

    protected void addProperties(StateDefinition.Builder<Block, BlockState> builder) {

    }

    @Override
    public int getMetaFromState(BlockState state) {
        return 0;
    }

    @Override
    public LegacyModelState getLegacyModelState(BlockState state, BlockGetter world, BlockPos pos) {
        Direction facing = WorldTools.getTile(world, pos, TileTorqueBase.class).map(TileTorqueBase::getPrimaryFacing).orElse(Direction.NORTH);
        return super.getLegacyModelState(state, world, pos).setValue(CoreProperties.UNLISTED_FACING, facing);
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
        WorldTools.getTile(world, pos, TileTorqueBase.class).ifPresent(TileTorqueBase::onNeighborTileChanged);
        super.onNeighborChange(state, world, pos, neighbor);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        WorldTools.getTile(world, pos, TileTorqueBase.class).ifPresent(TileTorqueBase::onNeighborTileChanged);
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
    }

    public boolean rotateBlock(Level world, BlockPos pos, Direction axis) {
        // Forge can invoke rotation on both logical sides; only the server may mutate block-entity facing.
        if (world.isClientSide) {
            return false;
        }
        //noinspection ConstantConditions
        IRotatableTile tt = WorldTools.getTile(world, pos, IRotatableTile.class).get();
        Direction facing = tt.getPrimaryFacing();
        Direction rotatedFacing = facing;
        if (axis.getAxis() == Direction.Axis.Y || getRotationType() == BlockRotationHandler.RotationType.SIX_WAY) {
            rotatedFacing = facing.getClockWise(axis.getAxis());
        }
        if (facing != rotatedFacing) {
            tt.setPrimaryFacing(rotatedFacing);
            return true;
        }
        return false;
    }

    @Override
    public boolean eventReceived(BlockState state, Level world, BlockPos pos, int id, int param) {
        return WorldTools.sendClientEventToTile(world, pos, id, param);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public int damageDropped(BlockState state) {
        return getMetaFromState(state);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        ModelLoaderHelper.registerItem(this, "automation", "normal");

        LegacyModelBakery.registerBlockKeyGenerator(this, new BlockStateKeyGenerator.Builder().addKeyProperties(CoreProperties.UNLISTED_FACING).addKeyProperties(AutomationProperties.DYNAMIC).addKeyProperties(o -> String.format("%.6f", o), AutomationProperties.ROTATIONS).build());
    }
}
