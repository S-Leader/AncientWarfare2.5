package net.shadowmage.ancientwarfare.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.input.IItemKeyInterface;
import net.shadowmage.ancientwarfare.core.input.IItemKeyInterface.ItemAltFunction;

public class PacketItemInteraction extends PacketBase {

    private byte altFunction;

    public PacketItemInteraction() {
    }

    public PacketItemInteraction(ItemAltFunction altFunction) {
        this.altFunction = (byte) altFunction.ordinal();
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        data.writeByte(altFunction);
    }

    @Override
    protected void readFromStream(ByteBuf data) {
        altFunction = data.readByte();
    }

    @Override
    protected void execute(Player player) {
        if (!executeKeyPress(player, InteractionHand.MAIN_HAND)) {
            executeKeyPress(player, InteractionHand.OFF_HAND);
        }
    }

    private boolean executeKeyPress(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            return false;
        }

        if (altFunction >= 0 && altFunction < ItemAltFunction.values().length && stack.getItem() instanceof IItemKeyInterface) {
            ((IItemKeyInterface) stack.getItem()).onKeyAction(player, stack, ItemAltFunction.values()[altFunction]);
            return true;
        }
        return false;
    }

}
