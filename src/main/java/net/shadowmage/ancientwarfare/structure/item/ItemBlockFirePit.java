package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import net.shadowmage.ancientwarfare.structure.block.BlockFirePit;

public class ItemBlockFirePit extends ItemBlockBase {
    public ItemBlockFirePit(Block block) {
        super(block);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        if (!stack.hasTag()) {
            return super.getDescriptionId(stack);
        }

        //noinspection ConstantConditions
        return String.format("%s.%s.%s", super.getDescriptionId(stack),
                stack.getTag().getString(BlockFirePit.VARIANT_TAG),
                stack.getTag().getBoolean(BlockFirePit.LIT_TAG) ? "lit" : "unlit");
    }
}
