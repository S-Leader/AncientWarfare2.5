package net.shadowmage.ancientwarfare.structure.block;


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
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.structure.gui.GuiDraftingStation;
import net.shadowmage.ancientwarfare.structure.tile.TileDraftingStation;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class BlockDraftingStation extends BlockBaseStructure {

    public BlockDraftingStation() {
        super(LegacyMaterial.ROCK, "drafting_station");
        setHardness(2.f);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(FACING, Direction.from2DDataValue(meta & 3));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(FACING).get2DDataValue();
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
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (!world.isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_DRAFTING_STATION, pos);
        }
        return true;
    }

    @Override
    public BlockState getStateForPlacement(Level worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        return defaultBlockState().setValue(FACING, placer.getDirection().getOpposite());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();
    }
}
