package net.shadowmage.ancientwarfare.automation.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableBlock;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableTile;
import net.shadowmage.ancientwarfare.core.interfaces.IBoundedSite;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import net.shadowmage.ancientwarfare.core.owner.IOwnable;
import net.shadowmage.ancientwarfare.core.util.BlockTools;

public class ItemBlockWorksiteStatic extends ItemBlockBase {

    public ItemBlockWorksiteStatic(Block block) {
        super(block);
    }

    @Override
    protected void onBlockPlaced(BlockPlaceContext context, BlockState placedState) {
        Player player = context.getPlayer();
        if (player == null) {
            return;
        }
        BlockPos pos = context.getClickedPos();
        Direction playerFacing = player.getDirection();
        BlockPos pos1 = pos.relative(playerFacing).relative(playerFacing.getCounterClockWise(), 2);
        BlockPos pos2 = pos.relative(playerFacing, 4).relative(playerFacing.getClockWise(), 4);
        BlockEntity worksite = context.getLevel().getBlockEntity(pos);
        if (worksite instanceof IBoundedSite boundedSite) {
            boundedSite.setBounds(pos1, pos2);
        }
        if (worksite instanceof IOwnable ownable) {
            ownable.setOwner(player);
        }
        if (worksite instanceof IRotatableTile rotatableTile && block instanceof IRotatableBlock rotatableBlock) {
            rotatableTile.setPrimaryFacing(BlockRotationHandler.getFaceForPlacement(player, rotatableBlock, context.getClickedFace()));
        }
        BlockTools.notifyBlockUpdate(context.getLevel(), pos);
    }
}
