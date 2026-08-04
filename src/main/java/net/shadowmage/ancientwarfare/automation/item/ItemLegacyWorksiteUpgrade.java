package net.shadowmage.ancientwarfare.automation.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.automation.AncientWarfareAutomation;
import net.shadowmage.ancientwarfare.automation.init.AWAutomationItems;
import net.shadowmage.ancientwarfare.core.item.ItemMulti;
import net.shadowmage.ancientwarfare.core.upgrade.WorksiteUpgrade;

/**
 * Hidden compatibility item for old ancientwarfareautomation:worksite_upgrade
 * stacks. Its Damage value is read only until the stack can be replaced by the
 * corresponding fixed-id item.
 */
public final class ItemLegacyWorksiteUpgrade extends ItemMulti {
    public ItemLegacyWorksiteUpgrade() {
        super(AncientWarfareAutomation.MOD_ID, "worksite_upgrade");
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level level, Player player, InteractionHand hand) {
        return ItemWorksiteUpgrade.useUpgrade(level, player, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof Player player)
                || slot < 0 || slot >= player.getInventory().getContainerSize()) {
            return;
        }

        WorksiteUpgrade upgrade = ItemWorksiteUpgrade.getUpgrade(stack);
        Item replacement = AWAutomationItems.getWorksiteUpgradeItem(upgrade);
        if (replacement == null) {
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