package net.shadowmage.ancientwarfare.automation.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;

public class ItemBlockWarehouseStorage extends ItemBlockBase {

    public ItemBlockWarehouseStorage(Block block) {
        super(block);
        this.setHasSubtypes(true);
    }

    @Override
    public String getDescriptionId(ItemStack par1ItemStack) {
        return super.getDescriptionId() + "." + par1ItemStack.getDamageValue();
    }

    @Override
    public int getMetadata(int par1) {
        return par1;
    }

}
