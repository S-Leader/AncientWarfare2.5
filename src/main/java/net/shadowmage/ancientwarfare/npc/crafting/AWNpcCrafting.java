package net.shadowmage.ancientwarfare.npc.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.init.AWNPCItems;

/**
 * Registers the four NBT-preserving order-copy recipes.
 */
@Mod.EventBusSubscriber(modid = AncientWarfareNPC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AWNpcCrafting {
    private static RecipeSerializer<OrderCopyingRecipe> upkeepSerializer;
    private static RecipeSerializer<OrderCopyingRecipe> routingSerializer;
    private static RecipeSerializer<OrderCopyingRecipe> combatSerializer;
    private static RecipeSerializer<OrderCopyingRecipe> workSerializer;

    private AWNpcCrafting() {
    }

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        event.register(ForgeRegistries.Keys.RECIPE_SERIALIZERS, helper -> {
            upkeepSerializer = register(helper, "upkeep_order_copy", AWNPCItems.UPKEEP_ORDER);
            routingSerializer = register(helper, "routing_order_copy", AWNPCItems.ROUTING_ORDER);
            combatSerializer = register(helper, "combat_order_copy", AWNPCItems.COMBAT_ORDER);
            workSerializer = register(helper, "work_order_copy", AWNPCItems.WORK_ORDER);
        });
    }

    private static RecipeSerializer<OrderCopyingRecipe> register(
            RegisterEvent.RegisterHelper<RecipeSerializer<?>> helper, String name, Item item) {
        SimpleCraftingRecipeSerializer<OrderCopyingRecipe> serializer =
                new SimpleCraftingRecipeSerializer<>((id, category) ->
                        new OrderCopyingRecipe(id, category, item, name));
        helper.register(new ResourceLocation(AncientWarfareNPC.MOD_ID, name), serializer);
        return serializer;
    }

    private static RecipeSerializer<?> serializerFor(String name) {
        return switch (name) {
            case "upkeep_order_copy" -> upkeepSerializer;
            case "routing_order_copy" -> routingSerializer;
            case "combat_order_copy" -> combatSerializer;
            case "work_order_copy" -> workSerializer;
            default -> throw new IllegalStateException("Unknown order-copy recipe: " + name);
        };
    }

    private static final class OrderCopyingRecipe extends CustomRecipe {
        private final Item item;
        private final String serializerName;

        private OrderCopyingRecipe(ResourceLocation id, CraftingBookCategory category, Item item, String serializerName) {
            super(id, category);
            this.item = item;
            this.serializerName = serializerName;
        }

        @Override
        public boolean matches(CraftingContainer container, Level level) {
            return findOrders(container) != null;
        }

        @Override
        public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
            ItemStack[] orders = findOrders(container);
            if (orders == null) {
                return ItemStack.EMPTY;
            }

            // The first order is the source; the second supplies the base output stack.
            ItemStack result = orders[1].copy();
            result.setTag(orders[0].hasTag() ? orders[0].getTag().copy() : null);
            result.setCount(2);
            return result;
        }

        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return width * height >= 2;
        }

        @Override
        public ItemStack getResultItem(RegistryAccess registryAccess) {
            return new ItemStack(item, 2);
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return serializerFor(serializerName);
        }

        private ItemStack[] findOrders(CraftingContainer container) {
            ItemStack first = ItemStack.EMPTY;
            ItemStack second = ItemStack.EMPTY;
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack stack = container.getItem(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                if (stack.getItem() != item) {
                    return null;
                }
                if (first.isEmpty()) {
                    first = stack;
                } else if (second.isEmpty()) {
                    second = stack;
                } else {
                    return null;
                }
            }
            return first.isEmpty() || second.isEmpty() ? null : new ItemStack[]{first, second};
        }
    }
}
