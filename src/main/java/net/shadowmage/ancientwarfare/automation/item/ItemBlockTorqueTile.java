package net.shadowmage.ancientwarfare.automation.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueBase;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.IRotatableBlock;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import net.shadowmage.ancientwarfare.core.owner.IOwnable;
import net.shadowmage.ancientwarfare.core.util.LegacyCreativeTabContents;

public class ItemBlockTorqueTile extends ItemBlockBase {
    private final IRotatableBlock rotatable;

    public ItemBlockTorqueTile(Block block) {
        super(block);
        if (!(block instanceof IRotatableBlock rotatableBlock)) {
            throw new IllegalArgumentException("Must be a rotatable block!!");
        }
        rotatable = rotatableBlock;
        //Must not build ItemStacks here — this runs during RegisterEvent, before registries are queryable.
        setHasSubtypes(LegacyCreativeTabContents.suppliesVariants(this));
    }

    @Override
    protected void onBlockPlaced(BlockPlaceContext context, BlockState placedState) {
        Player player = context.getPlayer();
        if (player == null) {
            return;
        }
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof TileTorqueBase tile) {
            if (tile instanceof IOwnable ownable) {
                ownable.setOwner(player);
            }
            tile.setPrimaryFacing(BlockRotationHandler.getFaceForPlacement(player, rotatable, context.getClickedFace()));
        }
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack) + "." + stack.getDamageValue();
    }
}
