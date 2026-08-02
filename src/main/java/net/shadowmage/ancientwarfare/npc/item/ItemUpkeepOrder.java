package net.shadowmage.ancientwarfare.npc.item;


import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.npc.orders.UpkeepOrder;

import java.util.Collections;
import java.util.List;

public class ItemUpkeepOrder extends ItemOrders {

    public ItemUpkeepOrder() {
        super("upkeep_order");
    }

    @Override
    public List<BlockPos> getPositionsForRender(ItemStack stack) {
        return UpkeepOrder.getUpkeepOrder(stack).map(o -> o.getUpkeepPosition().map(Collections::singletonList).orElse(Collections.emptyList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        if (!world.isClientSide)
            AWMenuTypes.open(player, NetworkHandler.GUI_NPC_UPKEEP_ORDER, hand.ordinal(), 0, 0);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @Override
    public void onKeyAction(Player player, ItemStack stack, ItemAltFunction altFunction) {
        UpkeepOrder.getUpkeepOrder(stack).ifPresent(upkeepOrder -> {
                    BlockPos hit = BlockTools.getBlockClickedOn(player, player.level(), false);
                    if (hit != null) {
                        if (upkeepOrder.addUpkeepPosition(player.level(), hit)) {
                            upkeepOrder.write(stack);
                            player.sendSystemMessage(Component.translatable("guistrings.npc.upkeep_point_set"));
                        }
                    } else
                        AWMenuTypes.open(player, NetworkHandler.GUI_NPC_UPKEEP_ORDER,
                                player.getOffhandItem() == stack ? InteractionHand.OFF_HAND.ordinal() : InteractionHand.MAIN_HAND.ordinal(), 0, 0);
                }
        );
    }

}
