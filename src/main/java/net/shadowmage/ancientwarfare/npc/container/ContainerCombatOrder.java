package net.shadowmage.ancientwarfare.npc.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.npc.orders.CombatOrder;

public class ContainerCombatOrder extends ContainerBase {

    private boolean hasChanged;
    private InteractionHand hand;
    public final CombatOrder combatOrder;

    public ContainerCombatOrder(Player player, int x, int y, int z) {
        super(player);
        this.hand = x == InteractionHand.OFF_HAND.ordinal() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Cannot open Combat Order GUI for empty stack/item.");
        }
        combatOrder = CombatOrder.getCombatOrder(stack);
        if (combatOrder == null) {
            throw new IllegalArgumentException("Combat orders was null for some reason");
        }
        addPlayerSlots();
        removeSlots();
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("combatOrder")) {
            combatOrder.deserializeNBT(tag.getCompound("combatOrder"));
            hasChanged = true;
        }
    }

    @Override
    public void onContainerClosed(Player par1EntityPlayer) {
        super.onContainerClosed(par1EntityPlayer);
        if (hasChanged && !player.level().isClientSide) {
            combatOrder.write(player.getItemInHand(hand));
        }
    }

    public void close() {
        CompoundTag outer = new CompoundTag();
        outer.put("combatOrder", combatOrder.serializeNBT());
        sendDataToServer(outer);
    }
}
