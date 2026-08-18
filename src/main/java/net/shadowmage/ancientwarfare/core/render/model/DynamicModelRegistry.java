package net.shadowmage.ancientwarfare.core.render.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Forge 1.20 model-bake hooks for the handful of AW blocks/items whose geometry
 * is still generated from runtime ModelData. Static items and blocks are loaded
 * normally from models/item and blockstates JSON and never pass through here.
 */
@Mod.EventBusSubscriber(modid = AncientWarfareCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DynamicModelRegistry {
    private static final Map<Block, DynamicBlockModel> BLOCK_MODELS = new ConcurrentHashMap<>();
    private static final Map<Item, DynamicItemModel> ITEM_MODELS = new ConcurrentHashMap<>();

    private DynamicModelRegistry() {
    }

    public static void registerBlock(Block block, LegacyBakery bakery,
                                     Function<BlockState, TextureAtlasSprite> particle) {
        BLOCK_MODELS.put(block, new DynamicBlockModel(bakery, particle));
    }

    public static void registerItem(Item item, LegacyBakery bakery, Supplier<TextureAtlasSprite> particle) {
        ITEM_MODELS.put(item, new DynamicItemModel(bakery, particle));
    }

    @SubscribeEvent
    public static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
        BLOCK_MODELS.forEach((block, definition) -> installBlockModels(event, block, definition));
        ITEM_MODELS.forEach((item, definition) -> installItemModel(event, item, definition));
    }

    private static void installBlockModels(ModelEvent.ModifyBakingResult event, Block block,
                                           DynamicBlockModel definition) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            event.getModels().put(BlockModelShaper.stateToModelLocation(state),
                    new LegacyBakedModel(definition.bakery(), () -> definition.particle().apply(state)));
        }
    }

    private static void installItemModel(ModelEvent.ModifyBakingResult event, Item item,
                                         DynamicItemModel definition) {
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
        if (itemId == null) {
            return;
        }

        ModelResourceLocation location = new ModelResourceLocation(itemId, "inventory");
        BakedModel jsonModel = event.getModels().get(location);
        if (jsonModel == null) {
            jsonModel = Minecraft.getInstance().getModelManager().getMissingModel();
        }

        LegacyBakedModel generatedModel = new LegacyBakedModel(definition.bakery(), definition.particle());
        BakedModel jsonBaseModel = jsonModel;
        ItemOverrides jsonOverrides = jsonBaseModel.getOverrides();

        // Install a real 3D inventory model as the baked model itself.  The old
        // implementation only returned the CCL model from ItemOverrides#resolve,
        // so renderers which consume the inventory baked model directly (including
        // several GUI/JEI-style paths) still saw the flat JSON icon.
        BakedModel generatedInventoryModel = generatedModel.forItem(new ItemStack(item));
        event.getModels().put(location, new BakedModelWrapper<BakedModel>(generatedInventoryModel) {
            private final ItemOverrides overrides = new ItemOverrides() {
                @Override
                public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                          @Nullable LivingEntity entity, int seed) {
                    BakedModel overridden = jsonOverrides.resolve(jsonBaseModel, stack, level, entity, seed);
                    if (overridden != null && overridden != jsonBaseModel) {
                        return overridden;
                    }
                    return generatedModel.forItem(stack);
                }
            };

            @Override
            public ItemOverrides getOverrides() {
                return overrides;
            }
        });
    }

    private record DynamicBlockModel(LegacyBakery bakery, Function<BlockState, TextureAtlasSprite> particle) {
    }

    private record DynamicItemModel(LegacyBakery bakery, Supplier<TextureAtlasSprite> particle) {
    }
}
