package net.shadowmage.ancientwarfare.core.input;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketItemInteraction;

@OnlyIn(Dist.CLIENT)
class ItemInputCallback implements IInputCallback {
    private final IItemKeyInterface.ItemAltFunction altFunction;

    ItemInputCallback(IItemKeyInterface.ItemAltFunction altFunction) {
        this.altFunction = altFunction;
    }

    @Override
    public void onKeyPressed() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) {
            return;
        }
        if (!runAction(minecraft, InteractionHand.MAIN_HAND)) {
            runAction(minecraft, InteractionHand.OFF_HAND);
        }
    }

    private boolean runAction(Minecraft minecraft, InteractionHand hand) {
        ItemStack stack = minecraft.player.getItemInHand(hand);
        if (stack.getItem() instanceof IItemKeyInterface && ((IItemKeyInterface) stack.getItem()).onKeyActionClient(minecraft.player, stack, altFunction)) {
            PacketItemInteraction pkt = new PacketItemInteraction(altFunction);
            NetworkHandler.sendToServer(pkt);
            return true;
        }
        return false;
    }
}
