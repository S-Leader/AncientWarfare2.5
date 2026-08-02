package net.shadowmage.ancientwarfare.core.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Common 1.20.1 block-item base with compatibility bridges for the old AW2
 * item-block hooks. Ownership/facing initialization runs after successful
 * placement through {@link #onBlockPlaced(BlockPlaceContext, BlockState)}.
 */
public class ItemBlockBase extends BlockItem {
    protected final Block block;
    private boolean legacyHasSubtypes;

    public ItemBlockBase(Block block) {
        super(block, new Item.Properties());
        this.block = block;
    }

    protected void setHasSubtypes(boolean hasSubtypes) {
        legacyHasSubtypes = hasSubtypes;
    }

    public boolean hasLegacySubtypes() {
        return legacyHasSubtypes;
    }

    public int getMetadata(int itemDamage) {
        return 0;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return onItemRightClick(level, player, hand);
    }

    public InteractionResultHolder<ItemStack> onItemRightClick(Level level, Player player, InteractionHand hand) {
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        InteractionResult legacyResult = onItemUse(context.getPlayer(), context.getLevel(), pos, context.getHand(),
                context.getClickedFace(), (float) (context.getClickLocation().x - pos.getX()),
                (float) (context.getClickLocation().y - pos.getY()), (float) (context.getClickLocation().z - pos.getZ()));
        return legacyResult == null ? super.useOn(context) : legacyResult;
    }

    /**
     * Return null to continue with normal BlockItem placement.
     */
    @Nullable
    public InteractionResult onItemUse(@Nullable Player player, Level level, BlockPos pos, InteractionHand hand,
                                       Direction face, float hitX, float hitY, float hitZ) {
        return null;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean placed = super.placeBlock(context, state);
        if (placed) {
            onBlockPlaced(context, context.getLevel().getBlockState(context.getClickedPos()));
        }
        return placed;
    }

    /**
     * Called only after the block was placed successfully.
     */
    protected void onBlockPlaced(BlockPlaceContext context, BlockState placedState) {
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        List<String> legacyTooltip = new ArrayList<>();
        try {
            addInformation(stack, level, legacyTooltip, flag);
        } catch (Exception ex) {
            //See ItemBase#appendHoverText: one broken legacy tooltip must not crash the client.
            net.shadowmage.ancientwarfare.core.AncientWarfareCore.LOG.error("Broken legacy tooltip on {}", getClass().getName(), ex);
            tooltip.add(Component.literal("[tooltip error: " + ex.getClass().getSimpleName() + "]"));
            return;
        }
        legacyTooltip.stream().map(Component::literal).forEach(tooltip::add);
    }

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
