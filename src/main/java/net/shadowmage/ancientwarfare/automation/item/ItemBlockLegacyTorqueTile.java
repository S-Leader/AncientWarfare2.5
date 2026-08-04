package net.shadowmage.ancientwarfare.automation.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.function.IntFunction;

/** Legacy torque BlockItem that preserves placement hooks while migrating old stacks. */
public final class ItemBlockLegacyTorqueTile extends ItemBlockTorqueTile {
    private final IntFunction<Item> replacementResolver;

    public ItemBlockLegacyTorqueTile(Block block, IntFunction<Item> replacementResolver) {
        super(block, true);
        this.replacementResolver = replacementResolver;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        ItemBlockLegacyVariant.migratePlayerStack(
                stack, level, entity, slot, replacementResolver.apply(stack.getDamageValue()));
    }
}
