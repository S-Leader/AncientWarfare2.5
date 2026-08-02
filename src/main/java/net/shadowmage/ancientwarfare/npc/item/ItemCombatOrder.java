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
import net.shadowmage.ancientwarfare.npc.orders.CombatOrder;

import java.util.ArrayList;
import java.util.List;

public class ItemCombatOrder extends ItemOrders {

    public ItemCombatOrder() {
        super("combat_order");
    }

    @Override
    public List<BlockPos> getPositionsForRender(ItemStack stack) {
        List<BlockPos> positionList = new ArrayList<>();
        CombatOrder order = CombatOrder.getCombatOrder(stack);
        if (order != null && !order.isEmpty()) {
            for (int i = 0; i < order.size(); i++) {
                positionList.add(order.get(i).above());
            }
        }
        return positionList;
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide)
            AWMenuTypes.open(player, NetworkHandler.GUI_NPC_COMBAT_ORDER, hand.ordinal(), 0, 0);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    public void onKeyAction(Player player, ItemStack stack, ItemAltFunction altFunction) {
        CombatOrder order = CombatOrder.getCombatOrder(stack);
        if (order == null) {
            return;
        }
        if (player.isShiftKeyDown()) {
            order.clear();
            order.write(stack);
        } else {
            BlockPos pos = BlockTools.getBlockClickedOn(player, player.level(), false);
            if (pos != null) {
                order.addPatrolPoint(player.level(), pos);
                order.write(stack);
                addMessage(player);
            }
        }
    }

}
