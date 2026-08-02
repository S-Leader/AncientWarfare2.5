package net.shadowmage.ancientwarfare.npc.item;


import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.RayTraceUtils;
import net.shadowmage.ancientwarfare.npc.orders.TradeOrder;

import java.util.ArrayList;
import java.util.List;

public class ItemTradeOrder extends ItemOrders {

    public ItemTradeOrder() {
        super("trade_order");
    }

    @Override
    public List<BlockPos> getPositionsForRender(ItemStack stack) {
        List<BlockPos> positionList = new ArrayList<>();
        TradeOrder order = TradeOrder.getTradeOrder(stack);
        if (order != null && order.getRoute().size() > 0) {
            for (int i = 0; i < order.getRoute().size(); i++) {
                positionList.add(order.getRoute().get(i).getPosition());
            }
        }
        return positionList;
    }

    @Override
    public boolean onKeyActionClient(Player player, ItemStack stack, ItemAltFunction altFunction) {
        return altFunction == ItemAltFunction.ALT_FUNCTION_1 || altFunction == ItemAltFunction.ALT_FUNCTION_2 || altFunction == ItemAltFunction.ALT_FUNCTION_3;
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide)
            AWMenuTypes.open(player, NetworkHandler.GUI_NPC_TRADE_ORDER, hand.ordinal(), 0, 0);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    public void onKeyAction(Player player, ItemStack stack, ItemAltFunction altFunction) {
        HitResult hit = RayTraceUtils.getPlayerTarget(player, 5, 0);
        if (!(hit instanceof BlockHitResult blockHit)) {
            return;
        }
        TradeOrder order = TradeOrder.getTradeOrder(stack);
        if (altFunction == ItemAltFunction.ALT_FUNCTION_1) {
            order.getRoute().addRoutePoint(blockHit.getBlockPos());
            order.write(stack);
        } else if (altFunction == ItemAltFunction.ALT_FUNCTION_2) {
            order.getRestockData().setDepositPoint(blockHit.getBlockPos(), blockHit.getDirection());
            order.write(stack);
        } else if (altFunction == ItemAltFunction.ALT_FUNCTION_3) {
            order.getRestockData().setWithdrawPoint(blockHit.getBlockPos(), blockHit.getDirection());
            order.write(stack);
        }
    }

}
