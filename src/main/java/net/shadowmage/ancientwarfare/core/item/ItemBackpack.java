package net.shadowmage.ancientwarfare.core.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.shadowmage.ancientwarfare.core.compat.CapabilityItemHandler;
import net.shadowmage.ancientwarfare.core.gui.GuiBackpack;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.inventory.ItemHandlerBackpack;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * A fixed-size backpack item.
 *
 * <p>Backpack capacity is part of the registered item id instead of legacy
 * item damage/NBT.  Only the actual inventory contents are serialized.</p>
 */
public class ItemBackpack extends ItemBaseCore {
    private static boolean guiRegistered;

    private final int rows;

    public ItemBackpack(String registryName, int rows) {
        super(registryName);
        if (rows < 1 || rows > 4) {
            throw new IllegalArgumentException("Backpack rows must be between 1 and 4");
        }
        this.rows = rows;
        setMaxStackSize(1);
    }

    public int getRows() {
        return rows;
    }

    public int getSlotCount() {
        return rows * 9;
    }

    public static boolean isBackpack(ItemStack stack) {
        return !stack.isEmpty()
                && (stack.getItem() instanceof ItemBackpack
                || stack.getItem() instanceof ItemLegacyBackpack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
        tooltip.add(I18n.get("guistrings.core.backpack.size", getSlotCount()));
        tooltip.add(I18n.get("guistrings.core.backpack.click_to_open"));
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_BACKPACK, 0, 0, 0);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        if (!guiRegistered) {
            guiRegistered = true;
        }
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            private final LazyOptional<ItemHandlerBackpack> handler =
                    LazyOptional.of(() -> new ItemHandlerBackpack(stack));

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
                if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    return handler.cast();
                }
                return LazyOptional.empty();
            }
        };
    }
}
