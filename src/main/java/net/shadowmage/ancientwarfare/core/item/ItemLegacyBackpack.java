package net.shadowmage.ancientwarfare.core.item;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.compat.CapabilityItemHandler;
import net.shadowmage.ancientwarfare.core.init.AWCoreItems;
import net.shadowmage.ancientwarfare.core.inventory.ItemHandlerBackpack;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Hidden compatibility item for old ancientwarfare:backpack stacks.
 * Player inventory stacks are converted to fixed-id backpacks on the first tick.
 * Stacks in legacy NPC/container data retain a working inventory capability until
 * they are moved through a player inventory and migrated.
 */
public final class ItemLegacyBackpack extends ItemBase implements IClientRegister {
    public ItemLegacyBackpack() {
        super(AncientWarfareCore.MOD_ID, "backpack");
        setMaxStackSize(1);
        setHasSubtypes(true);
        AncientWarfareCore.proxy.addClientRegister(this);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack) + "." + clampedMeta(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level level, Player player, InteractionHand hand) {
        ItemStack converted = convert(player.getItemInHand(hand));
        player.setItemInHand(hand, converted);
        if (converted.getItem() instanceof ItemBackpack backpack) {
            return backpack.onItemRightClick(level, player, hand);
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, converted);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !(entity instanceof Player player)
                || slot < 0 || slot >= player.getInventory().getContainerSize()) {
            return;
        }
        player.getInventory().setItem(slot, convert(stack));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<ItemHandlerBackpack> handler =
                    LazyOptional.of(() -> new ItemHandlerBackpack(stack));

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability,
                                                      @Nullable Direction facing) {
                if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    return handler.cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    private static int clampedMeta(ItemStack stack) {
        return Math.max(0, Math.min(3, stack.getDamageValue()));
    }

    private static ItemStack convert(ItemStack stack) {
        Item replacement = AWCoreItems.getBackpackByLegacyMeta(clampedMeta(stack));
        if (replacement == null) {
            return stack;
        }

        ItemStack converted = new ItemStack(replacement, stack.getCount());
        if (stack.hasTag()) {
            CompoundTag copied = stack.getTag().copy();
            copied.remove("Damage");
            if (!copied.isEmpty()) {
                converted.setTag(copied);
            }
        }
        return converted;
    }
}
