package net.shadowmage.ancientwarfare.core.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.init.AWCoreItems;

/**
 * Hidden compatibility entry for old saves containing ancientwarfare:component.
 * It is never used by recipes or the creative tab; player inventory stacks are
 * converted to the new per-component ids as soon as they tick.
 */
public final class ItemLegacyComponent extends ItemBase implements IClientRegister {
    public ItemLegacyComponent() {
        super(AncientWarfareCore.MOD_ID, "component");
        setHasSubtypes(true);
        AncientWarfareCore.proxy.addClientRegister(this);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack) + "." + stack.getDamageValue();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof Player player)) {
            return;
        }
        Item replacement = AWCoreItems.getComponentByLegacyMeta(stack.getDamageValue());
        if (replacement == null || slot < 0 || slot >= player.getInventory().getContainerSize()) {
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
