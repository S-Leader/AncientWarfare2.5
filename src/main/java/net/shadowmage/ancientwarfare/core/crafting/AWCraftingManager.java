package net.shadowmage.ancientwarfare.core.crafting;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.crafting.wrappers.NoRecipeWrapper;
import net.shadowmage.ancientwarfare.core.crafting.wrappers.RegularCraftingWrapper;
import net.shadowmage.ancientwarfare.core.crafting.wrappers.ResearchCraftingWrapper;
import net.shadowmage.ancientwarfare.core.research.ResearchTracker;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AWCraftingManager {
    private AWCraftingManager() {
    }

    private static final Map<ResourceLocation, ResearchRecipeBase> RESEARCH_RECIPES = new LinkedHashMap<>();
    private static final Map<ResourceLocation, ICraftingRecipe> REGULAR_RECIPE_CACHE = new HashMap<>();

    public static void init() {
        //noop - just call this so that the static final gets initialized at proper time
    }

    private static List<ICraftingRecipe> findMatchingResearchRecipes(CraftingContainer inventory, Level world, String playerName) {
        return findMatchingResearchRecipes(inventory, world, playerName, true);
    }

    private static List<ICraftingRecipe> findMatchingResearchRecipes(CraftingContainer inventory,
                                                                     @Nullable Level world, String playerName, boolean checkPlayerResearch) {
        List<ICraftingRecipe> ret = new ArrayList<>();
        if (world == null)
            return ret;
        for (ResearchRecipeBase recipe : RESEARCH_RECIPES.values()) {
            if (recipe.matches(inventory, world) && (!checkPlayerResearch || canPlayerCraft(recipe, world, playerName))) {
                ret.add(new ResearchCraftingWrapper(recipe));
            }
        }
        return ret;
    }

    private static List<ICraftingRecipe> findMatchingRegularRecipes(CraftingContainer inventory, @Nullable Level world) {
        List<ICraftingRecipe> ret = new ArrayList<>();
        if (world == null) {
            return ret;
        }
        for (Recipe<CraftingContainer> recipe : world.getRecipeManager().getRecipesFor(RecipeType.CRAFTING, inventory, world)) {
            ICraftingRecipe wrapper = new RegularCraftingWrapper(recipe, world.registryAccess());
            REGULAR_RECIPE_CACHE.put(recipe.getId(), wrapper);
            ret.add(wrapper);
        }
        return ret;
    }

    private static List<ICraftingRecipe> findMatchingRecipesNoResearchCheck(CraftingContainer inventory, Level world) {
        List<ICraftingRecipe> recipes = findMatchingResearchRecipes(inventory, world, "", false);
        recipes.addAll(findMatchingRegularRecipes(inventory, world));

        return recipes;
    }

    public static List<ICraftingRecipe> findMatchingRecipes(CraftingContainer inventory, Level world, String playerName) {
        List<ICraftingRecipe> recipes = findMatchingResearchRecipes(inventory, world, playerName);
        recipes.addAll(findMatchingRegularRecipes(inventory, world));

        return recipes;
    }

    public static boolean canPlayerCraft(Level world, String playerName, String research) {
        return ResearchTracker.INSTANCE.hasPlayerCompleted(world, playerName, research);
    }

    private static boolean canPlayerCraft(ResearchRecipeBase recipe, Level world, String playerName) {
        return !AWCoreStatics.useResearchSystem || canPlayerCraft(world, playerName, recipe.getNeededResearch());
    }

    static boolean addRecipe(ResearchRecipeBase recipe, boolean checkForExistence) {
        if (recipe.getId() != null && AWCoreStatics.isItemCraftable(recipe.getRecipeOutput().getItem())
                && (!checkForExistence || !RESEARCH_RECIPES.containsKey(recipe.getId()))) {
            RESEARCH_RECIPES.put(recipe.getId(), recipe);
            return true;
        }
        return false;
    }

    private static boolean hasCountIngredient(ResearchRecipeBase recipe) {
        return recipe.getIngredients().stream().anyMatch(i -> i instanceof IIngredientCount);
    }

    public static Collection<ResearchRecipeBase> getRecipes() {
        return RESEARCH_RECIPES.values();
    }

    public static void loadRecipes() {
        RESEARCH_RECIPES.clear();
        int loaded = ResearchRecipeLoader.load();
        AncientWarfareCore.LOG.info("Loaded {} Ancient Warfare research recipes", loaded);
    }

    public static void registerIngredients() {
        CraftingHelper.register(new ResourceLocation(AncientWarfareCore.MOD_ID, "item_count"), IngredientCount.SERIALIZER);
        CraftingHelper.register(new ResourceLocation(AncientWarfareCore.MOD_ID, "ore_dict_count"), IngredientOreCount.SERIALIZER);
    }

    public static ICraftingRecipe getRecipe(RecipeResourceLocation recipe) {
        switch (recipe.getRecipeType()) {
            case REGULAR:
                return REGULAR_RECIPE_CACHE.getOrDefault(recipe.getResourceLocation(), NoRecipeWrapper.INSTANCE);
            case RESEARCH:
                ResearchRecipeBase resRecipe = RESEARCH_RECIPES.get(recipe.getResourceLocation());
                return resRecipe != null ? new ResearchCraftingWrapper(resRecipe) : NoRecipeWrapper.INSTANCE;
            default:
                return NoRecipeWrapper.INSTANCE;
        }
    }

    public static CraftingContainer fillCraftingMatrixFromInventory(List<ItemStack> resources) {
        CraftingContainer invCrafting = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
            @Override
            public boolean stillValid(Player playerIn) {
                return true;
            }

            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }
        }, 3, 3);

        for (int i = 0; i < Math.min(resources.size(), invCrafting.getContainerSize()); i++) {
            invCrafting.setItem(i, resources.get(i));
        }

        return invCrafting;
    }

    public static ICraftingRecipe findMatchingRecipe(Level world, NonNullList<ItemStack> inputs, ItemStack result) {
        CraftingContainer inv = fillCraftingMatrixFromInventory(inputs);
        List<ICraftingRecipe> recipes = findMatchingRecipesNoResearchCheck(inv, world);

        if (recipes.isEmpty()) {
            return NoRecipeWrapper.INSTANCE;
        }

        //if there's only one match just return
        if (recipes.size() == 1) {
            return recipes.get(0);
        }

        //if there's multiple, filter by result and return the first that matches by result, just in case none match by result return the first recipe in the list
        return recipes.stream().filter(r -> recipeResultsEqual(result, r, inv)).findFirst().orElse(recipes.get(0));
    }

    private static boolean recipeResultsEqual(ItemStack result, ICraftingRecipe r, CraftingContainer inv) {
        ItemStack otherResult = r.getCraftingResult(inv);
        if (otherResult.isEmpty()) {
            return false;
        }

        return ItemStack.isSameItemSameTags(otherResult, result);
    }

    public static NonNullList<ItemStack> getRecipeInventoryMatch(ICraftingRecipe recipe, IItemHandler inventory) {
        return getRecipeInventoryMatch(recipe, NonNullList.create(), inventory);
    }

    public static NonNullList<ItemStack> getRecipeInventoryMatch(ICraftingRecipe recipe, List<ItemStack> exactStacks, IItemHandler inventory) {
        return getRecipeInventoryMatch(recipe, exactStacks, s -> InventoryTools.hasCountOrMore(inventory, s), inventory);
    }

    public static NonNullList<ItemStack> getRecipeInventoryMatch(ICraftingRecipe recipe, List<ItemStack> exactStacks, Function<ItemStack, Boolean> hasCountOrMore, IItemHandler inventory) {
        return getRecipeInventoryMatch(recipe, exactStacks, hasCountOrMore, inventory, () -> NonNullList.withSize(9, ItemStack.EMPTY), (list, slot, stack) -> {
            list.set(slot, stack);
            return list;
        }, (a, in) -> NonNullList.create());
    }

    public static <T> T getRecipeInventoryMatch(ICraftingRecipe recipe, IItemHandler inventory, Supplier<T> initialize, TriConsumer<T, Integer, ItemStack> onMatch, BiConsumer<T, Ingredient> onFail) {
        return getRecipeInventoryMatch(recipe, NonNullList.create(), s -> InventoryTools.hasCountOrMore(inventory, s), inventory, initialize, onMatch, onFail);
    }

    public static <T> T getRecipeInventoryMatch(ICraftingRecipe recipe, List<ItemStack> exactStacks, Function<ItemStack, Boolean> hasCountOrMore, IItemHandler inventory, Supplier<T> initialize, TriConsumer<T, Integer, ItemStack> onMatch, BiConsumer<T, Ingredient> onFail) {
        return getRecipeInventoryMatch(recipe, exactStacks, hasCountOrMore, inventory, initialize, (t, i, s) -> {
            onMatch.accept(t, i, s);
            return t;
        }, (t, in) -> {
            onFail.accept(t, in);
            return t;
        });
    }

    @SuppressWarnings("squid:UnusedPrivateMethod")
    private static <T> T getRecipeInventoryMatch(ICraftingRecipe recipe, List<ItemStack> exactStacks, Function<ItemStack, Boolean> hasCountOrMore, IItemHandler inventory, Supplier<T> initialize, TriFunction<T, Integer, ItemStack, T> onMatch, BiFunction<T, Ingredient, T> onFail) {
        T ret = initialize.get();

        if (!recipe.isValid()) {
            ret = onFail.apply(ret, Ingredient.EMPTY);
            return ret;
        }

        Map<Integer, IngredientMatchData> ingredientsData = getIngredientData(recipe.getIngredients());
        Map<Integer, Ingredient> remainingIngredients = getSlotIngredients(ingredientsData);

        ret = matchExactStacks(exactStacks, hasCountOrMore, onMatch, ret, ingredientsData, remainingIngredients);

        if (remainingIngredients.isEmpty()) {
            return ret;
        }

        ret = matchUsingRecipeIngredients(inventory, onMatch, ret, ingredientsData, remainingIngredients);

        ret = processFailedIngredients(onFail, ret, remainingIngredients);

        return ret;
    }

    private static <T> T matchUsingRecipeIngredients(IItemHandler inventory, TriFunction<T, Integer, ItemStack, T> onMatch, T ret, Map<Integer, IngredientMatchData> ingredientsData, Map<Integer, Ingredient> remainingIngredients) {
        for (int slot = 0; slot < inventory.getSlots() && !remainingIngredients.isEmpty(); slot++) {
            ItemStack resourceStack = inventory.getStackInSlot(slot);
            if (resourceStack.isEmpty()) {
                continue;
            }
            ret = matchIngredientsToStack(onMatch, ret, ingredientsData, remainingIngredients, resourceStack);
        }
        return ret;
    }

    private static <T> T matchIngredientsToStack(TriFunction<T, Integer, ItemStack, T> onMatch, T ret, Map<Integer, IngredientMatchData> ingredientsData, Map<Integer, Ingredient> remainingIngredients, ItemStack resourceStack) {
        int currentStackCount = resourceStack.getCount();

        Iterator<Map.Entry<Integer, Ingredient>> it = remainingIngredients.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Ingredient> entry = it.next();
            Ingredient ingredient = entry.getValue();

            if (ingredient.test(resourceStack)) {
                IngredientMatchData ingredientData = ingredientsData.get(entry.getKey());
                int countToRemove = Math.min(ingredientData.remainingCount, currentStackCount);
                currentStackCount -= countToRemove;
                ingredientData.remainingCount -= countToRemove;
                if (ingredientData.remainingCount < 1) {
                    ret = addSuccessfulMatch(ret, onMatch, resourceStack, ingredientData.count, ingredientData.index);
                    it.remove();
                }
                if (currentStackCount <= 0) {
                    break;
                }
            }
        }
        return ret;
    }

    private static <T> T matchExactStacks(List<ItemStack> exactStacks, Function<ItemStack, Boolean> hasCountOrMore, TriFunction<T, Integer, ItemStack, T> onMatch, T ret, Map<Integer, IngredientMatchData> ingredientsData, Map<Integer, Ingredient> remainingIngredients) {
        Map<ItemStack, Set<Integer>> consolidatedStacks = consolidateStacks(exactStacks, ingredientsData);
        for (Map.Entry<ItemStack, Set<Integer>> consolidatedStack : consolidatedStacks.entrySet()) {
            ItemStack stack = consolidatedStack.getKey();
            if (hasCountOrMore.apply(stack)) {
                for (Integer ingredientIndex : consolidatedStack.getValue()) {
                    remainingIngredients.remove(ingredientIndex);
                    if (ingredientsData.containsKey(ingredientIndex)) {
                        IngredientMatchData ingredientData = ingredientsData.get(ingredientIndex);
                        ret = addSuccessfulMatch(ret, onMatch, stack, ingredientData.count, ingredientData.index);
                    } else {
                        ret = addSuccessfulMatch(ret, onMatch, stack, 1, ingredientIndex);
                    }
                }
            } else {
                for (int ingredientIndex : consolidatedStack.getValue()) {
                    if (!remainingIngredients.containsKey(ingredientIndex)) {
                        IngredientCount ingredient = new IngredientCount(consolidatedStack.getKey()) {
                            @Override
                            public int getCount() {
                                return 1;
                            }
                        };
                        remainingIngredients.put(ingredientIndex, ingredient);
                        ingredientsData.put(ingredientIndex, getSlotIngredient(ingredientIndex, ingredient));
                    }
                }
            }
        }
        return ret;
    }

    private static <T> T addSuccessfulMatch(T ret, TriFunction<T, Integer, ItemStack, T> onMatch, ItemStack resourceStack, int count, int index) {
        ItemStack stackFound = resourceStack.copy();
        stackFound.setCount(count);
        ret = onMatch.apply(ret, index, stackFound);
        return ret;
    }

    private static Map<ItemStack, Set<Integer>> consolidateStacks(List<ItemStack> exactStacks, Map<Integer, IngredientMatchData> ingredientsData) {
        Map<ItemStack, Set<Integer>> ret = new HashMap<>();
        Set<Integer> matchedIngredientIndexes = new HashSet<>();
        int slot = 0;
        for (ItemStack stack : exactStacks) {
            consolidateStack(ingredientsData, ret, matchedIngredientIndexes, stack, slot);
            slot++;
        }

        return ret;
    }

    private static void consolidateStack(Map<Integer, IngredientMatchData> ingredientsData, Map<ItemStack, Set<Integer>> stackIngredientIndexes,
                                         Set<Integer> matchedIngredientIndexes, ItemStack stack, int slot) {
        if (stack.isEmpty()) {
            return;
        }
        int index = getIngredientIndexForStack(ingredientsData, matchedIngredientIndexes, stack);
        int count = 1;
        if (index == -1) {
            index = slot;
        } else {
            count = ingredientsData.get(index).count;
        }
        matchedIngredientIndexes.add(index);
        Optional<Map.Entry<ItemStack, Set<Integer>>> matching = getMatchingStackEntry(stackIngredientIndexes, stack);
        if (matching.isPresent()) {
            Map.Entry<ItemStack, Set<Integer>> entry = matching.get();
            entry.getKey().grow(count);
            entry.getValue().add(index);
        } else {
            Set<Integer> indexes = new HashSet<>();
            indexes.add(index);
            ItemStack stackCopy = stack.copy();
            stackCopy.setCount(count);
            stackIngredientIndexes.put(stackCopy, indexes);
        }
    }

    private static int getIngredientIndexForStack(Map<Integer, IngredientMatchData> ingredientsData, Set<Integer> matchedIngredientIndexes, ItemStack stack) {
        for (Map.Entry<Integer, IngredientMatchData> ingredientData : ingredientsData.entrySet()) {
            IngredientMatchData data = ingredientData.getValue();
            if (!matchedIngredientIndexes.contains(data.index) && data.ingredient.test(stack)) {
                return data.index;
            }
        }
        return -1;
    }

    private static Optional<Map.Entry<ItemStack, Set<Integer>>> getMatchingStackEntry(Map<ItemStack, Set<Integer>> stacks, ItemStack stackToFind) {
        for (Map.Entry<ItemStack, Set<Integer>> entry : stacks.entrySet()) {
            if (InventoryTools.doItemStacksMatch(entry.getKey(), stackToFind)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    private static Map<Integer, Ingredient> getSlotIngredients(Map<Integer, IngredientMatchData> ingredientsData) {
        Map<Integer, Ingredient> ret = new HashMap<>();
        ingredientsData.forEach((slot, ingredientData) -> ret.put(slot, ingredientData.ingredient));
        return ret;
    }

    private static <T> T processFailedIngredients(BiFunction<T, Ingredient, T> onFail, T ret, Map<Integer, Ingredient> remainingIngredients) {
        for (Ingredient ingredient : remainingIngredients.values()) {
            ret = onFail.apply(ret, ingredient);
        }
        return ret;
    }

    private static class IngredientMatchData {
        public Ingredient ingredient;
        public int index;
        public int count;
        public int remainingCount;

        private IngredientMatchData(Ingredient ingredient, int index, int count) {
            this.ingredient = ingredient;
            this.index = index;
            this.count = count;
            remainingCount = count;
        }
    }

    private static Map<Integer, IngredientMatchData> getIngredientData(NonNullList<Ingredient> ingredients) {
        Map<Integer, IngredientMatchData> ret = new LinkedHashMap<>();
        for (int slot = 0; slot < ingredients.size(); slot++) {
            Ingredient ingredient = ingredients.get(slot);

            if (ingredient.test(ItemStack.EMPTY)) { //skip empty ingredients
                continue;
            }

            ret.put(slot, getSlotIngredient(slot, ingredient));
        }

        return ret;
    }

    private static IngredientMatchData getSlotIngredient(int slot, Ingredient ingredient) {
        return new IngredientMatchData(ingredient, slot, ingredient instanceof IIngredientCount ? ((IIngredientCount) ingredient).getCount() : 1);
    }

    public static ItemStack getIngredientInventoryMatch(IItemHandler clonedResourceInventory, Ingredient ingredient) {
        ItemStack stackFound = ItemStack.EMPTY;
        int count = ingredient instanceof IIngredientCount ? ((IIngredientCount) ingredient).getCount() : 1;
        for (int slot = 0; slot < clonedResourceInventory.getSlots(); slot++) {
            ItemStack resourceStack = clonedResourceInventory.getStackInSlot(slot);

            if (!resourceStack.isEmpty()) {
                //required for ingredient to actually see proper count and say it's a good item
                //e.g. ingredient requires stack of 3 but inventory only has 3 stacks of 1 of the item - that's still a match for the recipe
                ItemStack properCountStack = resourceStack.copy();
                properCountStack.setCount(count);

                if (ingredient.test(properCountStack)) {
                    ItemStack removedStack = InventoryTools.removeItems(clonedResourceInventory, resourceStack, count, true);

                    if (removedStack.getCount() == count) {
                        InventoryTools.removeItems(clonedResourceInventory, resourceStack, count, false);
                        stackFound = removedStack;
                        break;
                    }
                }
            }
        }
        return stackFound;
    }

    public static NonNullList<ItemStack> getReusableStacks(ICraftingRecipe recipe, CraftingContainer craftMatrix) {
        return recipe.getRemainingItems(craftMatrix).stream().filter(s -> !s.isEmpty() && recipe.getIngredients().stream().anyMatch(i -> i.test(s)))
                .collect(Collectors.toCollection(NonNullList::create));
    }
}
