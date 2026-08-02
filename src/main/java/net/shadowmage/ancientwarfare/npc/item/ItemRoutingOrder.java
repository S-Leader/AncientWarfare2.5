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
import net.shadowmage.ancientwarfare.npc.orders.RoutingOrder;

import java.util.ArrayList;
import java.util.List;

public class ItemRoutingOrder extends ItemOrders {

    public ItemRoutingOrder() {
        super("routing_order");
    }

    @Override
    public List<BlockPos> getPositionsForRender(ItemStack stack) {
        List<BlockPos> positionList = new ArrayList<>();
        RoutingOrder order = RoutingOrder.getRoutingOrder(stack);
        if (order != null && !order.isEmpty()) {
            for (RoutingOrder.RoutePoint e : order.getEntries()) {
                positionList.add(e.getTarget());
            }
        }
        return positionList;
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide)
            AWMenuTypes.open(player, NetworkHandler.GUI_NPC_ROUTING_ORDER, hand.ordinal(), 0, 0);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    public void onKeyAction(Player player, ItemStack stack, ItemAltFunction altFunction) {
        RoutingOrder order = RoutingOrder.getRoutingOrder(stack);
        if (order != null) {
            HitResult hit = RayTraceUtils.getPlayerTarget(player, 5, 0);
            if (hit instanceof BlockHitResult blockHit) {
                order.addRoutePoint(blockHit.getDirection(), blockHit.getBlockPos());
                order.write(stack);
                addMessage(player);
            }
        }
    }

}
