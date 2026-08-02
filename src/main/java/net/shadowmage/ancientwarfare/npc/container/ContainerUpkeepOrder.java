package net.shadowmage.ancientwarfare.npc.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.npc.orders.UpkeepOrder;

import java.util.Optional;

public class ContainerUpkeepOrder extends ContainerBase {
    private static final String UPKEEP_ORDER_TAG = "upkeepOrder";
    private InteractionHand hand;
    public final UpkeepOrder upkeepOrder;
    public final ItemStack upkeepBlock;
    private boolean hasChanged;

    @SuppressWarnings("unused") //used in reflection
    public ContainerUpkeepOrder(Player player, int x, int y, int z) {
        super(player);
        this.hand = x == InteractionHand.OFF_HAND.ordinal() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Cannot open Work Order GUI for null stack/item.");
        }
        Optional<UpkeepOrder> order = UpkeepOrder.getUpkeepOrder(stack);
        if (!order.isPresent()) {
            throw new IllegalArgumentException("Upkeep orders was missing for some reason");
        }
        upkeepOrder = order.get();
        upkeepBlock = upkeepOrder.getUpkeepPosition().map(blockPos -> new ItemStack(player.level().getBlockState(blockPos).getBlock())).orElse(ItemStack.EMPTY);
        addPlayerSlots();
        removeSlots();
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains(UPKEEP_ORDER_TAG)) {
            upkeepOrder.deserializeNBT(tag.getCompound(UPKEEP_ORDER_TAG));
            hasChanged = true;
        }
    }

    @Override
    public void onContainerClosed(Player par1EntityPlayer) {
        super.onContainerClosed(par1EntityPlayer);
        if (hasChanged && !player.level().isClientSide) {
            upkeepOrder.write(player.getItemInHand(hand));
        }
    }

    public void onClose() {
        CompoundTag outer = new CompoundTag();
        outer.put(UPKEEP_ORDER_TAG, upkeepOrder.serializeNBT());
        sendDataToServer(outer);
    }
}
