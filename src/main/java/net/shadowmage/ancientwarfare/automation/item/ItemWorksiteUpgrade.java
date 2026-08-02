package net.shadowmage.ancientwarfare.automation.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.shadowmage.ancientwarfare.automation.AncientWarfareAutomation;
import net.shadowmage.ancientwarfare.automation.init.AWAutomationItems;
import net.shadowmage.ancientwarfare.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfare.core.item.ItemMulti;
import net.shadowmage.ancientwarfare.core.upgrade.WorksiteUpgrade;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.LegacyItemStack;

import java.util.HashSet;

public class ItemWorksiteUpgrade extends ItemMulti {

    public ItemWorksiteUpgrade() {
        super(AncientWarfareAutomation.MOD_ID, "worksite_upgrade");
    }

    private static WorksiteUpgrade getUpgrade(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != AWAutomationItems.WORKSITE_UPGRADE) {
            throw new RuntimeException("Cannot retrieve worksite upgrade type for: " + stack + ".  Null stack, or item, or mismatched item!");
        }
        return WorksiteUpgrade.values()[stack.getDamageValue()];
    }

    public static ItemStack getStack(WorksiteUpgrade upgrade) {
        return upgrade == null ? ItemStack.EMPTY : LegacyItemStack.of(AWAutomationItems.WORKSITE_UPGRADE, 1, upgrade.ordinal());
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (world.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        BlockPos pos = BlockTools.getBlockClickedOn(player, world, false);
        if (pos != null) {
            BlockEntity te = world.getBlockEntity(pos);
            if (te instanceof IWorkSite) {
                IWorkSite ws = (IWorkSite) te;
                WorksiteUpgrade upgrade = getUpgrade(stack);
                if (!ws.getValidUpgrades().contains(upgrade)) {
                    return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
                }
                HashSet<WorksiteUpgrade> wsug = new HashSet<>(ws.getUpgrades());
                if (wsug.contains(upgrade)) {
                    return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
                }
                for (WorksiteUpgrade ug : wsug) {
                    if (ug.exclusive(upgrade)) {
                        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);//exclusive upgrade present, exit early
                    }
                }
                for (WorksiteUpgrade ug : wsug) {
                    if (upgrade.overrides(ug)) {
                        InventoryTools.dropItemInWorld(player.level(), getStack(ug), te.getBlockPos());
                        ws.removeUpgrade(ug);
                    }
                }
                ws.addUpgrade(upgrade);
                stack.shrink(1);
            }
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
