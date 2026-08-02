package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import net.shadowmage.ancientwarfare.core.util.NBTBuilder;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;

public class ItemBlockGravestone extends ItemBlockBase {
    public ItemBlockGravestone(Block block) {
        super(block);
    }

    public static int getVariant(ItemStack stack) {
        //noinspection ConstantConditions
        return stack.hasTag() ? stack.getTag().getInt("variant") : 1;
    }

    public static ItemStack getVariantStack(int variant) {
        ItemStack stack = new ItemStack(AWStructureBlocks.GRAVESTONE);
        stack.setTag(new NBTBuilder().setInteger("variant", variant).build());
        return stack;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (stack.hasTag()) {
            String name = "tile.gravestone." + getVariant(stack) + ".name";
            return I18n.get(name);
        }

        return super.getItemStackDisplayName(stack);
    }
}
