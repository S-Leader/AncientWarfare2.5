package net.shadowmage.ancientwarfare.core.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared item base retaining the small set of 1.12 item hooks still used by
 * AW2. Modern Item entry points delegate to those hooks, allowing individual
 * items to be migrated without losing their existing behaviour.
 */
public abstract class ItemBase extends Item {
    private final ResourceLocation legacyRegistryName;
    private int legacyMaxStackSize = 64;
    private boolean legacyHasSubtypes;

    public ItemBase(String modID, String regName) {
        this(modID, regName, new Item.Properties());
    }

    public ItemBase(String modID, String regName, Item.Properties properties) {
        super(properties);
        legacyRegistryName = new ResourceLocation(modID, regName);
    }

    public ResourceLocation getLegacyRegistryName() {
        return legacyRegistryName;
    }

    /**
     * Source-compatible name retained for old AW item implementations.
     */
    public ResourceLocation getRegistryName() {
        return legacyRegistryName;
    }

    protected void setMaxStackSize(int size) {
        legacyMaxStackSize = Math.max(1, size);
    }

    protected void setHasSubtypes(boolean hasSubtypes) {
        legacyHasSubtypes = hasSubtypes;
    }

    public boolean hasLegacySubtypes() {
        return legacyHasSubtypes;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(super.getMaxStackSize(stack), legacyMaxStackSize);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return onItemRightClick(level, player, hand);
    }

    /**
     * Legacy 1.12 right-click hook.
     */
    public InteractionResultHolder<ItemStack> onItemRightClick(Level level, Player player, InteractionHand hand) {
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        return onItemUse(context.getPlayer(), context.getLevel(), pos, context.getHand(), context.getClickedFace(),
                (float) (context.getClickLocation().x - pos.getX()), (float) (context.getClickLocation().y - pos.getY()),
                (float) (context.getClickLocation().z - pos.getZ()));
    }

    /**
     * Legacy 1.12 block-use hook.
     */
    public InteractionResult onItemUse(@Nullable Player player, Level level, BlockPos pos, InteractionHand hand,
                                       Direction face, float hitX, float hitY, float hitZ) {
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        List<String> legacyTooltip = new ArrayList<>();
        try {
            addInformation(stack, level, legacyTooltip, flag);
        } catch (Exception ex) {
            //The creative search tree builds tooltips for every item at once; one broken
            //legacy tooltip must not take down the whole client during the port.
            net.shadowmage.ancientwarfare.core.AncientWarfareCore.LOG.error("Broken legacy tooltip on {}", getClass().getName(), ex);
            tooltip.add(Component.literal("[tooltip error: " + ex.getClass().getSimpleName() + "]"));
            return;
        }
        legacyTooltip.stream().map(Component::literal).forEach(tooltip::add);
    }

    /**
     * Legacy 1.12 tooltip hook.
     */
    public void addInformation(ItemStack stack, @Nullable Level level, List<String> tooltip, TooltipFlag flag) {
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(getItemStackDisplayName(stack));
    }

    public String getItemStackDisplayName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack)).getString();
    }

    public String getUnlocalizedNameInefficiently(ItemStack stack) {
        return getDescriptionId(stack);
    }

    public void getSubItems(CreativeModeTab tab, net.minecraft.core.NonNullList<ItemStack> items) {
    }
}
