package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import net.shadowmage.ancientwarfare.structure.block.BlockTotemPart;

public class ItemBlockTotemPart extends ItemBlockBase {
    public ItemBlockTotemPart(Block block) {
        super(block);
    }

    public static BlockTotemPart.Variant getVariant(ItemStack stack) {
        if (stack.hasTag()) {
            //noinspection ConstantConditions
            return BlockTotemPart.Variant.fromId(stack.getTag().getByte("variant"));
        }
        return BlockTotemPart.Variant.BASE;
    }

    @Override
    protected boolean canPlace(BlockPlaceContext context, BlockState state) {
        Player player = context.getPlayer();
        return (player == null || getVariant(context.getItemInHand()).canPlace(context.getLevel(), context.getClickedPos(), player))
                && super.canPlace(context, state);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        if (!stack.hasTag()) {
            return super.getDescriptionId(stack);
        }

        //noinspection ConstantConditions
        return String.format("%s.%s", super.getDescriptionId(stack),
                BlockTotemPart.Variant.fromId(stack.getTag().getByte("variant")).name().toLowerCase());
    }
}
