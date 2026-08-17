package net.shadowmage.ancientwarfare.core.item;


import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.gui.GuiResearchBook;
import net.shadowmage.ancientwarfare.core.init.AWCoreItems;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;

import javax.annotation.Nullable;
import java.util.List;

public class ItemResearchBook extends ItemBaseCore {

    public ItemResearchBook() {
        super("research_book");
        this.setMaxStackSize(1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level world, List<String> tooltip, TooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        String name = getResearcherName(stack);
        if (name == null) {
            tooltip.add(I18n.get("guistrings.research.researcher_name") + ": " + I18n.get("guistrings.research.no_researcher"));
            tooltip.add(I18n.get("guistrings.research.right_click_to_bind"));
        } else {
            tooltip.add(I18n.get("guistrings.research.researcher_name") + ": " + name);
            tooltip.add(I18n.get("guistrings.research.right_click_to_view"));
        }
    }

    @Nullable
    public static String getResearcherName(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() == AWCoreItems.RESEARCH_BOOK.get() && stack.hasTag() && stack.getTag().contains("researcherName")) {
            return stack.getTag().getString("researcherName");
        }
        return null;
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide) {
            if (!stack.hasTag() || !stack.getTag().contains("researcherName")) {
                stack.getOrCreateTag().put("researcherName", StringTag.valueOf(player.getName().getString()));
                player.sendSystemMessage(Component.translatable("guistrings.research.book_bound"));
            } else {
                AWMenuTypes.open(player, NetworkHandler.GUI_RESEARCH_BOOK, 0, 0, 0);
            }
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();
    }
}
