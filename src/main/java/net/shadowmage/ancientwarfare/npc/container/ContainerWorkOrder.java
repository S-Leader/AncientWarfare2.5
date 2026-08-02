package net.shadowmage.ancientwarfare.npc.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.npc.orders.WorkOrder;

public class ContainerWorkOrder extends ContainerBase {

    private InteractionHand hand;
    public final WorkOrder wo;
    private boolean hasChanged;

    public ContainerWorkOrder(Player player, int x, int y, int z) {
        super(player);
        hand = x == InteractionHand.OFF_HAND.ordinal() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Cannot open Work Order GUI for empty stack/item.");
        }
        wo = WorkOrder.getWorkOrder(stack);
        if (wo == null) {
            throw new IllegalArgumentException("Work orders was null for some reason");
        }
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("wo")) {
            wo.deserializeNBT(tag.getCompound("wo"));
            hasChanged = true;
        }
    }

    @Override
    public void onContainerClosed(Player par1EntityPlayer) {
        super.onContainerClosed(par1EntityPlayer);
        if (hasChanged && !player.level().isClientSide) {
            wo.write(player.getItemInHand(hand));
        }
    }

    public void onClose() {
        CompoundTag outer = new CompoundTag();
        outer.put("wo", wo.serializeNBT());
        sendDataToServer(outer);
    }
}
