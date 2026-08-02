package net.shadowmage.ancientwarfare.core.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableBlock;
import net.shadowmage.ancientwarfare.core.owner.IOwnable;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import javax.annotation.Nullable;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class ItemBlockOwnedRotatable extends ItemBlockBase {
    private final IRotatableBlock rotatable;

    public <T extends Block & IRotatableBlock> ItemBlockOwnedRotatable(T block) {
        super(block);
        rotatable = block;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        Player player = context.getPlayer();
        if (state == null || player == null || !state.hasProperty(FACING)) {
            return state;
        }
        return state.setValue(FACING, BlockRotationHandler.getFaceForPlacement(player, rotatable, context.getClickedFace()));
    }

    @Override
    protected void onBlockPlaced(BlockPlaceContext context, BlockState placedState) {
        Player player = context.getPlayer();
        if (player != null) {
            WorldTools.getTile(context.getLevel(), context.getClickedPos(), IOwnable.class)
                    .ifPresent(tile -> tile.setOwner(player));
        }
    }
}
