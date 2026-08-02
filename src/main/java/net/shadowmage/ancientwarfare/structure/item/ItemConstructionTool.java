package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.input.IItemKeyInterface;
import net.shadowmage.ancientwarfare.core.input.InputHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.LegacyBlockState;
import net.shadowmage.ancientwarfare.structure.event.IBoxRenderer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class ItemConstructionTool extends ItemBaseStructure implements IItemKeyInterface, IBoxRenderer {

    public ItemConstructionTool(String name) {
        super(name);
        setMaxStackSize(1);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level world, List<String> tooltip, TooltipFlag flagIn) {
        ConstructionSettings settings = getSettings(stack);
        if (settings == null) {
            return;
        }
        String text = I18n.get("guistrings.construction.mode") + ": " + settings.type;
        tooltip.add(text);
        text = I18n.get("guistrings.construction.fill_block") + ": "
                + (settings.blockState == null ? "" : ForgeRegistries.BLOCKS.getKey(settings.blockState.getBlock()));
        tooltip.add(text);

        String keyText;
        text = "RMB" + " = " + I18n.get("guistrings.construction.do_action");
        tooltip.add(text);

        keyText = InputHandler.ALT_ITEM_USE_1.getTranslatedKeyMessage().getString();
        text = keyText + " = " + I18n.get("guistrings.construction.toggle_mode");
        tooltip.add(text);

        keyText = InputHandler.ALT_ITEM_USE_2.getTranslatedKeyMessage().getString();
        text = keyText + " = " + I18n.get("guistrings.construction.set_fill_block");
        tooltip.add(text);

        keyText = InputHandler.ALT_ITEM_USE_3.getTranslatedKeyMessage().getString();
        text = keyText + " = " + I18n.get("guistrings.construction.set_pos_1");
        tooltip.add(text);

        keyText = InputHandler.ALT_ITEM_USE_4.getTranslatedKeyMessage().getString();
        text = keyText + " = " + I18n.get("guistrings.construction.set_pos_2");
        tooltip.add(text);

        keyText = InputHandler.ALT_ITEM_USE_5.getTranslatedKeyMessage().getString();
        text = keyText + " = " + I18n.get("guistrings.construction.clear_positions");
        tooltip.add(text);
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (world.isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        ConstructionSettings settings = getSettings(stack);
        if (settings == null) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
        switch (settings.type) {
            case SOLID_FILL: {
                handleSolidFill(player, settings);
            }
            break;
            case BOX_FILL: {
                handleBoxFill(player, settings);
            }
            break;
            case LAKE_FILL: {
                handleLakeFill(player, settings);
            }
            break;
            case LAYER_FILL: {
                handleLayerFill(player, settings);
            }
            break;
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    private void handleSolidFill(Player player, ConstructionSettings settings) {
        if (settings.pos1 != null && settings.pos2 != null && settings.blockState != null) {
            BlockPos min = BlockTools.getMin(settings.pos1, settings.pos2);
            BlockPos max = BlockTools.getMax(settings.pos1, settings.pos2);
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    for (int y = min.getY(); y <= max.getY(); y++) {
                        player.level().setBlock(new BlockPos(x, y, z), settings.blockState, 3);
                    }
                }
            }
        }
    }

    private void handleBoxFill(Player player, ConstructionSettings settings) {
        if (settings.pos1 != null && settings.pos2 != null && settings.blockState != null) {
            BlockPos min = BlockTools.getMin(settings.pos1, settings.pos2);
            BlockPos max = BlockTools.getMax(settings.pos1, settings.pos2);

            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    player.level().setBlock(new BlockPos(x, max.getY(), z), settings.blockState, 3);
                    player.level().setBlock(new BlockPos(x, min.getY(), z), settings.blockState, 3);
                }
            }
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    player.level().setBlock(new BlockPos(x, y, min.getZ()), settings.blockState, 3);
                    player.level().setBlock(new BlockPos(x, y, max.getZ()), settings.blockState, 3);
                }
            }
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    player.level().setBlock(new BlockPos(min.getX(), y, z), settings.blockState, 3);
                    player.level().setBlock(new BlockPos(max.getX(), y, z), settings.blockState, 3);
                }
            }
        }
    }

    private void handleLakeFill(Player player, ConstructionSettings settings) {
        if (settings.blockState != null) {
            BlockPos pos = BlockTools.getBlockClickedOn(player, player.level(), player.isShiftKeyDown());
            if (pos != null) {
                BlockState state = player.level().getBlockState(pos);
                Block block = state.getBlock();
                Set<BlockPos> toFill = new FloodFillPathfinder(player.level(), pos, block, state, false, true).doFloodFill();
                for (BlockPos p1 : toFill) {
                    player.level().setBlock(p1, settings.blockState, 3);
                }
            }
        }
    }

    private void handleLayerFill(Player player, ConstructionSettings settings) {
        if (settings.blockState != null) {
            BlockPos pos = BlockTools.getBlockClickedOn(player, player.level(), player.isShiftKeyDown());
            if (pos != null) {
                BlockState state = player.level().getBlockState(pos);
                Block block = state.getBlock();
                Set<BlockPos> toFill = new FloodFillPathfinder(player.level(), pos, block, state, false, false).doFloodFill();
                for (BlockPos p1 : toFill) {
                    player.level().setBlock(p1, settings.blockState, 3);
                }
            }
        }
    }

    @Override
    public boolean onKeyActionClient(Player player, ItemStack stack, ItemAltFunction altFunction) {
        return true;
    }

    @Override
    public void onKeyAction(Player player, ItemStack stack, ItemAltFunction altFunction) {
        ConstructionSettings settings = getSettings(stack);
        if (settings == null) {
            return;
        }
        switch (altFunction) {
            case ALT_FUNCTION_1://toggle mode
            {
                settings.type = settings.type.next();
            }
            break;
            case ALT_FUNCTION_2://source block
            {
                BlockPos pos = BlockTools.getBlockClickedOn(player, player.level(), player.isShiftKeyDown());
                if (pos != null) {
                    BlockState state = player.level().getBlockState(pos);
                    settings.blockState = state;
                    writeConstructionSettings(stack, settings);
                }
            }
            return;
            case ALT_FUNCTION_3://pos1
            {
                BlockPos pos = BlockTools.getBlockClickedOn(player, player.level(), player.isShiftKeyDown());
                if (pos != null) {
                    settings.pos1 = pos;
                    writeConstructionSettings(stack, settings);
                }
            }
            return;
            case ALT_FUNCTION_4://pos2
            {
                BlockPos pos = BlockTools.getBlockClickedOn(player, player.level(), player.isShiftKeyDown());
                if (pos != null) {
                    settings.pos2 = pos;
                    writeConstructionSettings(stack, settings);
                }
            }
            return;
            case ALT_FUNCTION_5://clear pos
            {
                settings.pos1 = null;
                settings.pos2 = null;
            }
            break;
        }
        writeConstructionSettings(stack, settings);
    }

    public static ConstructionSettings getSettings(ItemStack item) {
        if (item.getItem() instanceof ItemConstructionTool) {
            ConstructionSettings settings = new ConstructionSettings();
            if (item.hasTag() && item.getTag().contains("constructionSettings")) {
                settings.readFromNBT(item.getTag().getCompound("constructionSettings"));
            }
            return settings;
        }
        return null;
    }

    public static void writeConstructionSettings(ItemStack item, ConstructionSettings settings) {
        if (item.getItem() instanceof ItemConstructionTool) {
            item.getOrCreateTag().put("constructionSettings", settings.writeToNBT(new CompoundTag()));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderBox(Player player, InteractionHand hand, ItemStack stack, float delta) {
        ConstructionSettings settings = getSettings(stack);
        if (settings == null) {
            return;
        }
        BlockPos p1, p2;
        BlockPos p3 = BlockTools.getBlockClickedOn(player, player.level(), player.isShiftKeyDown());
        p1 = settings.hasPos1() ? settings.pos1() : p3;
        p2 = settings.hasPos2() ? settings.pos2() : p3;
        if (p1 != null && p2 != null) {
            Util.renderBoundingBox(player, BlockTools.getMin(p1, p2), BlockTools.getMax(p1, p2), delta);
        }
        if (p3 != null) {
            Util.renderBoundingBox(player, p3, p3, delta, 1, 0, 0);
        }
    }

    public static final class ConstructionSettings {
        @Nullable
        BlockState blockState;
        BlockPos pos1;
        BlockPos pos2;
        ConstructionType type = ConstructionType.SOLID_FILL;

        protected void readFromNBT(CompoundTag tag) {
            if (tag.contains("pos1")) {
                pos1 = BlockPos.of(tag.getLong("pos1"));
            }
            if (tag.contains("pos2")) {
                pos2 = BlockPos.of(tag.getLong("pos2"));
            }
            if (tag.contains("blockState", Tag.TAG_COMPOUND)) {
                blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("blockState"));
            } else if (tag.contains("block")) {
                ResourceLocation blockName = ResourceLocation.tryParse(tag.getString("block"));
                Block legacyBlock = blockName != null && ForgeRegistries.BLOCKS.containsKey(blockName)
                        ? ForgeRegistries.BLOCKS.getValue(blockName) : null;
                if (legacyBlock != null) {
                    blockState = LegacyBlockState.fromMeta(legacyBlock, tag.getInt("meta"));
                }
            }
            if (tag.contains("type")) {
                type = ConstructionType.get(tag.getInt("type"));
            }
        }

        protected CompoundTag writeToNBT(CompoundTag tag) {
            if (blockState != null) {
                tag.put("blockState", NbtUtils.writeBlockState(blockState));
            }
            if (pos1 != null) {
                tag.putLong("pos1", pos1.asLong());
            }
            if (pos2 != null) {
                tag.putLong("pos2", pos2.asLong());
            }
            tag.putInt("type", type.ordinal());
            return tag;
        }

        public boolean hasPos1() {
            return pos1 != null;
        }

        public boolean hasPos2() {
            return pos2 != null;
        }

        public BlockPos pos1() {
            return pos1;
        }

        public BlockPos pos2() {
            return pos2;
        }

    }

    public enum ConstructionType {

        /*
         * Fills current layer and downwards with chosen block
         */
        LAKE_FILL,

        /*
         * fills current layer only with chosen block
         */
        LAYER_FILL,

        /*
         * fills entire bounding box with chosen block
         */
        SOLID_FILL,

        /*
         * creates a box around chosen area with chosen block
         */
        BOX_FILL;

        public ConstructionType next() {
            int ordinal = ordinal();
            ordinal++;
            if (ordinal >= values().length) {
                ordinal = 0;
            }
            return values()[ordinal];
        }

        public static ConstructionType get(int index) {
            if (index >= 0 && index < values().length) {
                return values()[index];
            }
            return SOLID_FILL;
        }
    }

}
