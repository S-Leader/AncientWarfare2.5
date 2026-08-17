package net.shadowmage.ancientwarfare.core.item;


import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.gamedata.WorldData;
import net.shadowmage.ancientwarfare.core.gui.manual.GuiManual;
import net.shadowmage.ancientwarfare.core.init.AWCoreItems;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;

import javax.annotation.Nullable;
import java.util.List;

public class ItemManual extends ItemBaseCore {
    public ItemManual() {
        super("manual");
        setMaxStackSize(1);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        AWMenuTypes.open(player, NetworkHandler.GUI_MANUAL);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("item.manual.tooltip"));
    }

    @SubscribeEvent
    public void handlePlayerFirstAWCraft(PlayerEvent.ItemCraftedEvent evt) {
        Player player = evt.getEntity();

        if (player.level().isClientSide) {
            return;
        }

        Item item = evt.getCrafting().getItem();
        var itemId = ForgeRegistries.ITEMS.getKey(item);
        if (item != AWCoreItems.MANUAL.get() && itemId != null && itemId.getNamespace().startsWith("ancientwarfare")) {
            WorldData data = AWGameData.INSTANCE.getPerWorldData(player.level(), WorldData.class);
            if (!data.wasPlayerGivenManual(player)) {
                ItemEntity manualDrop = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), new ItemStack(AWCoreItems.MANUAL.get()));
                manualDrop.setPickUpDelay(0);
                player.level().addFreshEntity(manualDrop);
                data.addPlayerThatWasGivenManual(player);
            }
        }
    }
}
