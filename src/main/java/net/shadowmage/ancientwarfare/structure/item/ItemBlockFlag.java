package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import org.apache.commons.lang3.StringUtils;

public class ItemBlockFlag extends ItemBlockBase {
    public ItemBlockFlag(Block block) {
        super(block);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        String name = "";
        if (stack.hasTag()) {
            //noinspection ConstantConditions
            name = StringUtils.capitalize(stack.getTag().getString("name"));
        }

        return I18n.get(getUnlocalizedNameInefficiently(stack) + ".name", name).trim();
    }
}
