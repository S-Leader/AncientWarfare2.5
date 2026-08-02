package net.shadowmage.ancientwarfare.core.crafting;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.shadowmage.ancientwarfare.core.util.LegacyOreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class IngredientOreCount extends Ingredient implements IIngredientCount {
    public static final IIngredientSerializer<IngredientOreCount> SERIALIZER = new Serializer();
    private final String ore;
    private final int count;

    public IngredientOreCount(String ore, int count) {
        super(values(ore));
        this.ore = ore;
        this.count = count;
    }

    private static Stream<? extends Value> values(String ore) {
        List<Value> values = new ArrayList<>();
        values.add(new TagValue(toTag(ore)));
        LegacyOreDictionary.getOres(ore).stream().map(ItemValue::new).forEach(values::add);
        fallbackItems(ore).map(ItemValue::new).forEach(values::add);
        return values.stream();
    }

    /**
     * Vanilla entries whose 1.12 ore names do not have a dependable Forge tag in 1.20.1.
     */
    private static Stream<ItemStack> fallbackItems(String ore) {
        return switch (ore) {
            case "blockCactus" -> Stream.of(new ItemStack(Items.CACTUS));
            case "chest" -> Stream.of(new ItemStack(Items.CHEST), new ItemStack(Items.TRAPPED_CHEST));
            case "cobblestone" -> Stream.of(new ItemStack(Items.COBBLESTONE));
            case "cropWheat" -> Stream.of(new ItemStack(Items.WHEAT));
            case "dustRedstone" -> Stream.of(new ItemStack(Items.REDSTONE));
            case "dyeBlack" -> Stream.of(new ItemStack(Items.BLACK_DYE));
            case "dyeBlue" -> Stream.of(new ItemStack(Items.BLUE_DYE));
            case "dyeGray" -> Stream.of(new ItemStack(Items.GRAY_DYE));
            case "dyeGreen" -> Stream.of(new ItemStack(Items.GREEN_DYE));
            case "dyeLightGray" -> Stream.of(new ItemStack(Items.LIGHT_GRAY_DYE));
            case "dyeRed" -> Stream.of(new ItemStack(Items.RED_DYE));
            case "dyeWhite" -> Stream.of(new ItemStack(Items.WHITE_DYE));
            case "dyeYellow" -> Stream.of(new ItemStack(Items.YELLOW_DYE));
            case "enderpearl" -> Stream.of(new ItemStack(Items.ENDER_PEARL));
            case "feather" -> Stream.of(new ItemStack(Items.FEATHER));
            case "fenceWood" -> Stream.of(new ItemStack(Items.OAK_FENCE));
            case "gemDiamond" -> Stream.of(new ItemStack(Items.DIAMOND));
            case "gemEmerald" -> Stream.of(new ItemStack(Items.EMERALD));
            case "gunpowder" -> Stream.of(new ItemStack(Items.GUNPOWDER));
            case "ingotGold" -> Stream.of(new ItemStack(Items.GOLD_INGOT));
            case "ingotIron" -> Stream.of(new ItemStack(Items.IRON_INGOT));
            case "leather" -> Stream.of(new ItemStack(Items.LEATHER));
            case "logWood" -> Stream.of(new ItemStack(Items.OAK_LOG));
            case "netherrack" -> Stream.of(new ItemStack(Items.NETHERRACK));
            case "obsidian" -> Stream.of(new ItemStack(Items.OBSIDIAN));
            case "paneGlass" -> Stream.of(new ItemStack(Items.GLASS_PANE));
            case "paper" -> Stream.of(new ItemStack(Items.PAPER));
            case "plankWood" -> Stream.of(new ItemStack(Items.OAK_PLANKS));
            case "sand" -> Stream.of(new ItemStack(Items.SAND), new ItemStack(Items.RED_SAND));
            case "stickWood" -> Stream.of(new ItemStack(Items.STICK));
            case "stone" -> Stream.of(new ItemStack(Items.STONE));
            case "string" -> Stream.of(new ItemStack(Items.STRING));
            case "torch" -> Stream.of(new ItemStack(Items.TORCH));
            case "workbench" -> Stream.of(new ItemStack(Items.CRAFTING_TABLE));
            default -> Stream.empty();
        };
    }

    private static TagKey<Item> toTag(String ore) {
        if ("plankWood".equals(ore)) {
            return TagKey.create(Registries.ITEM, new ResourceLocation("minecraft", "planks"));
        }
        String snake = ore.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
        String[] parts = snake.split("_", 2);
        String path = parts.length == 2 ? plural(parts[0]) + "/" + parts[1] : snake;
        return TagKey.create(Registries.ITEM, new ResourceLocation("forge", path));
    }

    private static String plural(String value) {
        return value.endsWith("s") ? value : value + "s";
    }

    @Override
    public ItemStack[] getItems() {
        return Arrays.stream(super.getItems()).map(stack -> stack.copyWithCount(count)).toArray(ItemStack[]::new);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        return input != null && input.getCount() >= count && super.test(input);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    private static class Serializer implements IIngredientSerializer<IngredientOreCount> {
        @Override
        public IngredientOreCount parse(FriendlyByteBuf buffer) {
            return new IngredientOreCount(buffer.readUtf(), buffer.readVarInt());
        }

        @Override
        public IngredientOreCount parse(JsonObject json) {
            return new IngredientOreCount(GsonHelper.getAsString(json, "ore"), GsonHelper.getAsInt(json, "count", 1));
        }

        @Override
        public void write(FriendlyByteBuf buffer, IngredientOreCount ingredient) {
            buffer.writeUtf(ingredient.ore);
            buffer.writeVarInt(ingredient.count);
        }
    }
}
