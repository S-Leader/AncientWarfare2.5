package net.shadowmage.ancientwarfare.automation.block;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.gui.GuiMailboxInventory;
import net.shadowmage.ancientwarfare.automation.tile.TileMailbox;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableBlock;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RotationType;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class BlockMailbox extends BlockBaseAutomation implements IRotatableBlock {

    public BlockMailbox(String regName) {
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
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public BlockEntity createTileEntity(Level world, BlockState state) {
        return new TileMailbox();
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (!world.isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_MAILBOX_INVENTORY, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    public boolean rotateBlock(Level world, BlockPos pos, Direction axis) {
        if (world.isClientSide)
            return false;
        BlockState state = world.getBlockState(pos);
        Direction facing = state.getValue(FACING);
        Direction rotatedFacing = facing.getClockWise(axis.getAxis());
        if (facing != rotatedFacing) {
            world.setBlock(pos, state.setValue(FACING, rotatedFacing), 3);
            return true;
        }
        return false;
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Override
    public boolean invertFacing() {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(Level worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        return defaultBlockState().setValue(FACING, placer.getDirection());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();

        NetworkHandler.registerGui(NetworkHandler.GUI_MAILBOX_INVENTORY, GuiMailboxInventory.class);
    }
}
