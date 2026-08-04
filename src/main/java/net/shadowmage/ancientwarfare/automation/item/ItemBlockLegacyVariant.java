package net.shadowmage.ancientwarfare.automation.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;

import java.util.function.IntFunction;

/** Hidden compatibility BlockItem for old metadata-based automation stacks. */
public class ItemBlockLegacyVariant extends ItemBlockBase {
    private final IntFunction<Item> replacementResolver;

    public ItemBlockLegacyVariant(Block block, IntFunction<Item> replacementResolver) {
        super(block);
        this.replacementResolver = replacementResolver;
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int itemDamage) {
        return itemDamage;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack) + "." + stack.getDamageValue();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        migratePlayerStack(stack, level, entity, slot, replacementResolver.apply(stack.getDamageValue()));
    }

    static void migratePlayerStack(ItemStack stack, Level level, Entity entity, int slot, Item replacement) {
        if (level.isClientSide || replacement == null || !(entity instanceof Player player)
                || slot < 0 || slot >= player.getInventory().getContainerSize()) {
            return;
        }
        ItemStack converted = new ItemStack(replacement, stack.getCount());
        if (stack.hasTag()) {
            CompoundTag copied = stack.getTag().copy();
            copied.remove("Damage");
            if (!copied.isEmpty()) {
                converted.setTag(copied);
            }
        }
        player.getInventory().setItem(slot, converted);
    }
}
