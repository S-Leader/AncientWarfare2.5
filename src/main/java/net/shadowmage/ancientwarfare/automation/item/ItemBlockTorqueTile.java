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
    private final boolean legacyVariants;

    public ItemBlockTorqueTile(Block block) {
        this(block, true);
    }

    public ItemBlockTorqueTile(Block block, boolean legacyVariants) {
        super(block);
        this.legacyVariants = legacyVariants;
        if (!(block instanceof IRotatableBlock rotatableBlock)) {
            throw new IllegalArgumentException("Must be a rotatable block!!");
        }
        rotatable = rotatableBlock;
        //Must not build ItemStacks here — this may run while registry population is still in progress, before registries are queryable.
        setHasSubtypes(legacyVariants && LegacyCreativeTabContents.suppliesVariants(this));
    }

    @Override
    protected void onBlockPlaced(BlockPlaceContext context, BlockState placedState) {
        Player player = context.getPlayer();
        if (player == null || context.getLevel().isClientSide) {
            return;
        }

        /*
         * BlockItem placement runs on both logical sides.  Torque facing is block-entity
         * state and must only be mutated by the server.  Calling setChanged()/block
         * updates from the client while the just-placed shaft is still being inserted
         * into the render/chunk graph can re-enter the dynamic shaft renderer and crash
         * immediately on placement.  The server update packet supplies the facing to
         * the client after placement.
         */
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof TileTorqueBase tile) {
            if (tile instanceof IOwnable ownable) {
                ownable.setOwner(player);
            }
            tile.setPrimaryFacing(BlockRotationHandler.getFaceForPlacement(player, rotatable, context.getClickedFace()));
        }
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return legacyVariants
                ? super.getDescriptionId(stack) + "." + stack.getDamageValue()
                : super.getDescriptionId(stack);
    }
}
