package net.shadowmage.ancientwarfare.npc.item;


import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.npc.orders.WorkOrder;

import java.util.ArrayList;
import java.util.List;

public class ItemWorkOrder extends ItemOrders {
    public ItemWorkOrder() {
        super("work_order");
    }

    @Override
    public List<BlockPos> getPositionsForRender(ItemStack stack) {
        List<BlockPos> positionList = new ArrayList<>();
        WorkOrder order = WorkOrder.getWorkOrder(stack);
        if (order != null && !order.isEmpty()) {
            for (WorkOrder.WorkEntry e : order.getEntries()) {
                positionList.add(e.getPosition());
            }
        }
        return positionList;
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide)
            AWMenuTypes.open(player, NetworkHandler.GUI_NPC_WORK_ORDER, hand.ordinal(), 0, 0);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    public void onKeyAction(Player player, ItemStack stack, ItemAltFunction altFunction) {
        WorkOrder wo = WorkOrder.getWorkOrder(stack);
        if (wo != null) {
            BlockPos hit = BlockTools.getBlockClickedOn(player, player.level(), false);
            if (wo.addWorkPosition(player.level(), hit)) {
                wo.write(stack);
                addMessage(player);
            } else {
                AWMenuTypes.open(player, NetworkHandler.GUI_NPC_WORK_ORDER,
                        player.getOffhandItem() == stack ? InteractionHand.OFF_HAND.ordinal() : InteractionHand.MAIN_HAND.ordinal(), 0, 0);
            }
        }
    }

}
