package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.automation.tile.TileChunkLoaderSimple;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

public class BlockChunkLoaderSimple extends BlockBaseAutomation {
    public BlockChunkLoaderSimple(String regName) {
        super(LegacyMaterial.ROCK, regName);
        setHardness(2.f);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


    @Override
    public void breakBlock(Level world, BlockPos pos, BlockState state) {
        WorldTools.getTile(world, pos, TileChunkLoaderSimple.class).ifPresent(TileChunkLoaderSimple::releaseForcedChunks);
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        return WorldTools.clickInteractableTileWithHand(world, pos, player, hand);
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        if (!world.isClientSide) {
            WorldTools.getTile(world, pos, TileChunkLoaderSimple.class).ifPresent(TileChunkLoaderSimple::setupInitialTicket);
        }
    }
}
