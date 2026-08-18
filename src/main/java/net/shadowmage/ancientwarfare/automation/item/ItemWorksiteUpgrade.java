package net.shadowmage.ancientwarfare.automation.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.shadowmage.ancientwarfare.automation.AncientWarfareAutomation;
import net.shadowmage.ancientwarfare.automation.init.AWAutomationItems;
import net.shadowmage.ancientwarfare.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfare.core.item.ItemBase;
import net.shadowmage.ancientwarfare.core.upgrade.WorksiteUpgrade;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import javax.annotation.Nullable;
import java.util.HashSet;

/** A single registry-backed worksite upgrade. */
public final class ItemWorksiteUpgrade extends ItemBase {
    private final WorksiteUpgrade upgrade;

    public ItemWorksiteUpgrade(String registryName, WorksiteUpgrade upgrade) {
        super(AncientWarfareAutomation.MOD_ID, registryName);
        this.upgrade = upgrade;
    }

    public WorksiteUpgrade getUpgrade() {
        return upgrade;
    }


    @Nullable
    public static WorksiteUpgrade getUpgrade(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        if (stack.getItem() instanceof ItemWorksiteUpgrade fixedUpgrade) {
            return fixedUpgrade.getUpgrade();
        }
        return null;
    }

    public static ItemStack getStack(WorksiteUpgrade upgrade) {
        Item item = AWAutomationItems.getWorksiteUpgradeItem(upgrade);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level level, Player player, InteractionHand hand) {
        return useUpgrade(level, player, hand);
    }

    static InteractionResultHolder<ItemStack> useUpgrade(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        WorksiteUpgrade upgrade = getUpgrade(stack);
        if (upgrade == null) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        if (level.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        BlockPos pos = BlockTools.getBlockClickedOn(player, level, false);
        if (pos == null) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof IWorkSite workSite)) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        if (!workSite.getValidUpgrades().contains(upgrade)) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        HashSet<WorksiteUpgrade> installedUpgrades = new HashSet<>(workSite.getUpgrades());
        if (installedUpgrades.contains(upgrade)) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        for (WorksiteUpgrade installed : installedUpgrades) {
            if (installed.exclusive(upgrade)) {
                return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
            }
        }

        for (WorksiteUpgrade installed : installedUpgrades) {
            if (upgrade.overrides(installed)) {
                InventoryTools.dropItemInWorld(player.level(), getStack(installed), blockEntity.getBlockPos());
                workSite.removeUpgrade(installed);
            }
        }

        workSite.addUpgrade(upgrade);
        stack.shrink(1);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
