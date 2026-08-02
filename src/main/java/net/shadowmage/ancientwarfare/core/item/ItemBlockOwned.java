package net.shadowmage.ancientwarfare.core.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.owner.IOwnable;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

public class ItemBlockOwned extends ItemBlockBase {

    public ItemBlockOwned(Block block) {
        super(block);
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
