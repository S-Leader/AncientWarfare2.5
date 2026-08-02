package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import net.shadowmage.ancientwarfare.structure.util.WoodVariantHelper;

public class WoodItemBlock extends ItemBlockBase {
    public WoodItemBlock(Block block) {
        super(block);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack) + "_" + WoodVariantHelper.getVariant(stack).getName();
    }
}
