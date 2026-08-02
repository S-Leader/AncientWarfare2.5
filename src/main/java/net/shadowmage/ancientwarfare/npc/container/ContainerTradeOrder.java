package net.shadowmage.ancientwarfare.npc.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.npc.orders.TradeOrder;

public class ContainerTradeOrder extends ContainerBase {

    private InteractionHand hand;
    public final TradeOrder orders;

    public ContainerTradeOrder(Player player, int x, int y, int z) {
        super(player);
        this.hand = x == InteractionHand.OFF_HAND.ordinal() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        orders = TradeOrder.getTradeOrder(player.getItemInHand(hand));

        addPlayerSlots((256 - (9 * 18)) / 2, 240 - 4 - 8 - 4 * 18, 4);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("tradeOrder")) {
            orders.deserializeNBT(tag.getCompound("tradeOrder"));
        }
    }

    @Override
    public void onContainerClosed(Player par1EntityPlayer) {
        if (!player.level().isClientSide) {
            orders.write(player.getItemInHand(hand));
        }
        super.onContainerClosed(par1EntityPlayer);
    }

    public void onClose() {
        CompoundTag outer = new CompoundTag();
        outer.put("tradeOrder", orders.serializeNBT());
        sendDataToServer(outer);
    }
}
