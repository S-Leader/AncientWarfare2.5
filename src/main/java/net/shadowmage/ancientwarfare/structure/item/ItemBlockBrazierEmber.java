package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import net.shadowmage.ancientwarfare.structure.block.BlockBrazierEmber;

public class ItemBlockBrazierEmber extends ItemBlockBase {
    public ItemBlockBrazierEmber(Block block) {
        super(block);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        if (!stack.hasTag()) {
            return super.getDescriptionId(stack);
        }

        //noinspection ConstantConditions
        return String.format("%s.%s", super.getDescriptionId(stack),
                stack.getTag().getBoolean(BlockBrazierEmber.LIT_TAG) ? "lit" : "unlit");
    }
}
