package net.shadowmage.ancientwarfare.core.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.registry.ResearchRegistry;
import net.shadowmage.ancientwarfare.core.research.ResearchGoal;
import net.shadowmage.ancientwarfare.core.research.ResearchTracker;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ItemResearchNotes extends ItemBaseCore {

    private static final String RESEARCH_NAME_TAG = "researchName";
    private NonNullList<ItemStack> displayCache = null;

    public ItemResearchNotes() {
        super("research_note");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level world, List<String> tooltip, TooltipFlag flagIn) {
        CompoundTag tag = stack.getTag();
        String researchName = "corrupt_item";
        boolean known = false;
        if (tag != null && tag.contains(RESEARCH_NAME_TAG)) {
            String name = tag.getString(RESEARCH_NAME_TAG);
            if (ResearchRegistry.researchExists(name) && Minecraft.getInstance().player != null && world != null) {
                researchName = I18n.get(ResearchGoal.getDescriptionId(name));
                known = ResearchTracker.INSTANCE.hasPlayerCompleted(world, Minecraft.getInstance().player.getName().getString(), name);
            } else {
                researchName = "missing_goal_for_id_" + researchName;
            }
        }
        tooltip.add(researchName);
        if (known) {
            tooltip.add(I18n.get("guistrings.research.known_research"));
            tooltip.add(I18n.get("guistrings.research.click_to_add_progress"));
        } else {
            tooltip.add(I18n.get("guistrings.research.unknown_research"));
            tooltip.add(I18n.get("guistrings.research.click_to_learn"));
        }
    }

    public void getSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (displayCache != null && !displayCache.isEmpty()) {
            items.addAll(displayCache);
            return;
        }
        displayCache = NonNullList.create();

        for (ResearchGoal goal : ResearchRegistry.getAllResearchGoals().stream().sorted(Comparator.comparing(ResearchGoal::getName))
                .collect(Collectors.toCollection(LinkedHashSet::new))) {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().put(RESEARCH_NAME_TAG, StringTag.valueOf(goal.getName()));
            displayCache.add(stack);
            items.add(stack);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getTag();
        if (!world.isClientSide && tag != null && tag.contains(RESEARCH_NAME_TAG)) {
            String name = tag.getString(RESEARCH_NAME_TAG);
            if (ResearchRegistry.researchExists(name)) {
                boolean known = ResearchTracker.INSTANCE.hasPlayerCompleted(player.level(), player.getName().getString(), name);
                if (!known) {
                    if (ResearchTracker.INSTANCE.addResearchFromNotes(player.level(), player.getName().getString(), name)) {
                        player.sendSystemMessage(Component.translatable("guistrings.research.learned_from_item", Component.translatable(name)));
                        stack.shrink(1);
                    }
                } else {
                    if (ResearchTracker.INSTANCE.addProgressFromNotes(player.level(), player.getName().getString(), name)) {
                        player.sendSystemMessage(Component.translatable("guistrings.research.added_progress", Component.translatable(name)));
                        stack.shrink(1);
                    }
                }
            }
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
