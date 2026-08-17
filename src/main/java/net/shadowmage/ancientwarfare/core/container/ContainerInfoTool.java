package net.shadowmage.ancientwarfare.core.container;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.init.AWCoreItems;
import net.shadowmage.ancientwarfare.core.util.EntityTools;

public class ContainerInfoTool extends ContainerBase {
    private final ItemStack infoTool;

    @SuppressWarnings("unused") //parameters used in reflection
    public ContainerInfoTool(Player player, int x, int y, int z) {
        super(player);

        infoTool = player.getItemInHand(EntityTools.getHandHoldingItem(player, AWCoreItems.INFO_TOOL.get()));

        addPlayerSlots(8);
    }

    public void printItemInfo(int slotId) {
        if (slotId >= 0 && slotId < inventoryItemStacks.size()) {
            ItemStack stack = inventorySlots.get(slotId).getItem();
            if (!stack.isEmpty()) {
                AWCoreItems.INFO_TOOL.get().printItemInfo(player, infoTool, stack);
            }
        }
    }
}
