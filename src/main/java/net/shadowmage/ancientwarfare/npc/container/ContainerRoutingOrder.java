package net.shadowmage.ancientwarfare.npc.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.npc.orders.RoutingOrder;

public class ContainerRoutingOrder extends ContainerBase {

    private boolean hasChanged;
    private InteractionHand hand;
    public final RoutingOrder routingOrder;

    public ContainerRoutingOrder(Player player, int x, int y, int z) {
        super(player);
        this.hand = x == InteractionHand.OFF_HAND.ordinal() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Cannot open Routing Order GUI for empty stack/item.");
        }
        routingOrder = RoutingOrder.getRoutingOrder(stack);
        if (routingOrder == null) {
            throw new IllegalArgumentException("Routing orders was null for some reason");
        }

        addPlayerSlots((256 - (9 * 18)) / 2, 240 - 4 * 18 - 8 - 4, 4);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("routingOrder")) {
            routingOrder.deserializeNBT(tag.getCompound("routingOrder"));
            hasChanged = true;
        }
    }

    @Override
    public void onContainerClosed(Player par1EntityPlayer) {
        super.onContainerClosed(par1EntityPlayer);
        if (hasChanged && !player.level().isClientSide) {
            routingOrder.write(player.getItemInHand(hand));
        }
    }

    public void onClose() {
        CompoundTag outer = new CompoundTag();
        outer.put("routingOrder", routingOrder.serializeNBT());
        sendDataToServer(outer);
    }
}
