package net.shadowmage.ancientwarfare.core.item;


import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.gui.GuiInfoTool;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.ItemTools;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ItemInfoTool extends ItemBaseCore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public ItemInfoTool() {
        super("info_tool", new Item.Properties().stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(getMode(stack).getDisplayName() + " mode"));
    }

    private void printSimpleMessage(Player player, BlockState state) {
        player.sendSystemMessage(Component.literal("Block name: " + BuiltInRegistries.BLOCK.getKey(state.getBlock())));
        if (!state.getValues().isEmpty()) {
            player.sendSystemMessage(Component.literal("Properties:"));
            for (Map.Entry<Property<?>, Comparable<?>> prop : state.getValues().entrySet()) {
                player.sendSystemMessage(Component.literal(prop.getKey().getName() + " : " + prop.getValue()));
            }
        }
    }

    private void printJSON(Player player, BlockState state) {
        printJson(player, BlockTools.serializeToJson(state).toString());
    }

    public void printItemInfo(Player player, ItemStack infoTool, ItemStack stack) {
        switch (getMode(infoTool)) {
            case INFO -> printSimpleMessage(player, stack);
            case JSON -> printJSON(player, stack);
            case LOOT_ENTRY -> printLootEntryJSON(player, stack);
        }
    }

    private void printSimpleMessage(Player player, ItemStack stack) {
        player.sendSystemMessage(Component.literal("Item name: " + BuiltInRegistries.ITEM.getKey(stack.getItem())));
        player.sendSystemMessage(Component.literal("Damage: " + stack.getDamageValue()));
        if (stack.hasTag()) player.sendSystemMessage(Component.literal("NBT: " + stack.getTag()));
    }

    private void printJSON(Player player, ItemStack stack) {
        printJson(player, ItemTools.serializeToJson(stack).toString());
    }

    private void printJson(Player player, String json) {
        player.sendSystemMessage(Component.literal(json));
        player.sendSystemMessage(Component.literal("JSON printed; click-drag the chat text to copy it."));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack tool = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.pass(tool);

        HitResult hit = player.pick(5.0D, 1.0F, false);
        if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
            BlockState state = level.getBlockState(blockHit.getBlockPos());
            switch (getMode(tool)) {
                case INFO -> printSimpleMessage(player, state);
                case JSON -> printJSON(player, state);
                case LOOT_ENTRY ->
                        printLootEntryJSON(player, state.getBlock().getCloneItemStack(state, blockHit, level, blockHit.getBlockPos(), player));
            }
            return InteractionResultHolder.success(tool);
        }

        if (player.isShiftKeyDown()) {
            return InteractionResultHolder.success(cycleMode(tool));
        }
        AWMenuTypes.open(player, NetworkHandler.GUI_INFO_TOOL);
        return InteractionResultHolder.success(tool);
    }

    private void printLootEntryJSON(Player player, ItemStack stack) {
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        JsonArray functions = new JsonArray();
        if (stack.getCount() != 1) {
            JsonObject count = new JsonObject();
            count.addProperty("function", "minecraft:set_count");
            count.addProperty("count", stack.getCount());
            functions.add(count);
        }
        if (stack.hasTag()) {
            JsonObject nbt = new JsonObject();
            nbt.addProperty("function", "minecraft:set_nbt");
            nbt.addProperty("tag", stack.getTag().toString());
            functions.add(nbt);
        }
        if (!functions.isEmpty()) entry.add("functions", functions);
        printJson(player, GSON.toJson(entry));
    }

    private ItemStack cycleMode(ItemStack stack) {
        stack.getOrCreateTag().putString("mode", getMode(stack).cycle().name().toLowerCase());
        return stack;
    }

    private Mode getMode(ItemStack stack) {
        return stack.hasTag() ? Mode.fromString(stack.getTag().getString("mode")) : Mode.INFO;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();
    }

    enum Mode {
        INFO("Info"), JSON("JSON"), LOOT_ENTRY("Loot Entry");
        private final String displayName;
        private static final Map<String, Mode> NAME_MODE_MAP;

        Mode(String displayName) {
            this.displayName = displayName;
        }

        public Mode cycle() {
            return values()[(ordinal() + 1) % values().length];
        }

        public static Mode fromString(String mode) {
            return NAME_MODE_MAP.getOrDefault(mode.toLowerCase(), INFO);
        }

        public String getDisplayName() {
            return displayName;
        }

        static {
            ImmutableMap.Builder<String, Mode> builder = ImmutableMap.builder();
            for (Mode mode : values()) builder.put(mode.name().toLowerCase(), mode);
            NAME_MODE_MAP = builder.build();
        }
    }
}
