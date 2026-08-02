package net.shadowmage.ancientwarfare.core.input;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IScrollableItem {
    boolean onScrollUp(Level world, Player player, ItemStack stack);

    boolean onScrollDown(Level world, Player player, ItemStack stack);
}
