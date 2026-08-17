package net.shadowmage.ancientwarfare.npc.block;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.npc.tile.TileTownHall;

public class BlockTownHall extends BlockBaseNPC {

    public BlockTownHall() {
        super(LegacyMaterial.ROCK, "town_hall");
        setHardness(2.f);
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
    @OnlyIn(Dist.CLIENT)
    public RenderType getBlockLayer() {
        return RenderType.cutout();
    }

    @Override
    public boolean isOpaqueCube(BlockState iBlockState) {
        return false;
    }

    /*
     * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
     * their own) Args: x, y, z, neighbor Block
     */

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!world.isClientSide) {
            WorldTools.getTile(world, pos, TileTownHall.class).ifPresent(t -> t.alarmActive = world.getBestNeighborSignal(pos) > 0);
        }
    }
}
