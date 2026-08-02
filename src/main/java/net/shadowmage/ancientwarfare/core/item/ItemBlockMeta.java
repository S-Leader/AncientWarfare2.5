package net.shadowmage.ancientwarfare.core.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class ItemBlockMeta extends ItemBlockBase {

    public ItemBlockMeta(Block block) {
        super(block);
        this.setHasSubtypes(true);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack) + "." + stack.getDamageValue();
    }

    @Override
    public int getMetadata(int itemDamage) {
        return itemDamage;
    }

}
