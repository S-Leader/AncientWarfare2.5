package net.shadowmage.ancientwarfare.core.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableBlock;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableTile;
import net.shadowmage.ancientwarfare.core.owner.IOwnable;
import net.shadowmage.ancientwarfare.core.util.BlockTools;

public class ItemBlockRotatableMetaTile extends ItemBlockBase {

    private final IRotatableBlock rotatable;

    public ItemBlockRotatableMetaTile(Block block) {
        super(block);
        if (!(block instanceof IRotatableBlock rotatableBlock)) {
            throw new IllegalArgumentException("Must be a rotatable block!!");
        }
        rotatable = rotatableBlock;
    }

    @Override
    protected void onBlockPlaced(BlockPlaceContext context, BlockState placedState) {
        Player player = context.getPlayer();
        if (player == null) {
            return;
        }
        BlockEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
        if (tile instanceof IOwnable ownable) {
            ownable.setOwner(player);
        }
        if (tile instanceof IRotatableTile rotatableTile) {
            rotatableTile.setPrimaryFacing(BlockRotationHandler.getFaceForPlacement(player, rotatable, context.getClickedFace()));
        }
        BlockTools.notifyBlockUpdate(context.getLevel(), context.getClickedPos());
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack) + "." + stack.getDamageValue();
    }
}
