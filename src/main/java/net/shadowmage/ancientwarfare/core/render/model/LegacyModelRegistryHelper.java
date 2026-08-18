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
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = AncientWarfareCore.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class LegacyModelRegistryHelper {
    private static final Map<ModelResourceLocation, BakedModel> MODELS = new ConcurrentHashMap<>();
    private static final Map<Item, Object> ITEM_RENDERERS = new ConcurrentHashMap<>();
    private static final Map<Item, DirectItemBakery> DIRECT_ITEM_BAKERIES = new ConcurrentHashMap<>();

    private LegacyModelRegistryHelper() {
    }

    public static void register(ModelResourceLocation location, BakedModel model) {
        MODELS.put(location, model);
    }

    public static void registerItemRenderer(Item item, Object renderer) {
        ITEM_RENDERERS.put(item, renderer);
    }

    public static void registerDirectItemBakery(Item item, LegacyBakery bakery, Supplier<TextureAtlasSprite> particle) {
        DIRECT_ITEM_BAKERIES.put(item, new DirectItemBakery(bakery, particle));
    }

    @SubscribeEvent
    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        // Pre-baked compatibility models are injected in ModifyBakingResult; asking
        // the vanilla bakery to load their legacy variant names only creates bogus
        // blockstate lookups before those replacements are installed.
        // Forge 1.20 treats non-inventory ModelResourceLocations as block-state
        // requests. Register only real standalone item models here; legacy block
        // variants are installed directly during ModifyBakingResult below.
        LegacyModelLoader.ITEM_VARIANTS.stream()
                .filter(LegacyModelRegistryHelper::isStandaloneItemModel)
                .forEach(event::register);
        LegacyModelLoader.ITEM_MODELS.values().stream()
                .filter(LegacyModelRegistryHelper::isStandaloneItemModel)
                .forEach(event::register);
    }

    private static boolean isStandaloneItemModel(ResourceLocation location) {
        return !(location instanceof ModelResourceLocation modelLocation)
                || "inventory".equals(modelLocation.getVariant());
    }

    @SubscribeEvent
    public static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
        event.getModels().putAll(MODELS);
        LegacyModelLoader.STATE_MAPPERS.forEach((block, mapper) -> installBakeryModels(event, block, mapper));
        installMissingVariantFallbacks(event);
        installLegacyItemModels(event);
        installDirectItemBakeries(event);
        reportMissingItemModels(event);
    }

    /**
     * A 1.12 item could point straight at a block-state variant. Modern item
     * baking does not load those legacy variant keys, so use the item's valid
     * inventory model when no dedicated baked variant exists. This keeps old
     * metadata items visible instead of returning the purple missing model.
     */
    private static void installMissingVariantFallbacks(ModelEvent.ModifyBakingResult event) {
        BakedModel missingModel = Minecraft.getInstance().getModelManager().getMissingModel();
        int installed = 0;
        for (ResourceLocation location : LegacyModelLoader.ITEM_VARIANTS) {
            BakedModel variant = event.getModels().get(location);
            if (variant != null && variant != missingModel) {
                continue;
            }
            ResourceLocation baseId = new ResourceLocation(location.getNamespace(), location.getPath());
            BakedModel base = event.getModels().get(new ModelResourceLocation(baseId, "inventory"));
            if (base == null || base == missingModel) {
                for (Item owner : LegacyModelLoader.ITEM_VARIANT_OWNERS.getOrDefault(location, java.util.Set.of())) {
                    ResourceLocation ownerId = ForgeRegistries.ITEMS.getKey(owner);
                    if (ownerId == null) {
                        continue;
                    }
                    base = event.getModels().get(new ModelResourceLocation(ownerId, "inventory"));
                    if (base != null && base != missingModel) {
                        break;
                    }
                }
            }
            if (base != null && base != missingModel) {
                event.getModels().put(location, base);
                installed++;
            }
        }
        AncientWarfareCore.LOG.info("Installed {} visible fallbacks for unbaked legacy item variants", installed);
    }

    private static void installDirectItemBakeries(ModelEvent.ModifyBakingResult event) {
        DIRECT_ITEM_BAKERIES.forEach((item, definition) -> {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
            if (itemId == null) {
                return;
            }
            ModelResourceLocation baseLocation = new ModelResourceLocation(itemId, "inventory");
            BakedModel original = event.getModels().get(baseLocation);
            if (original == null) {
                original = Minecraft.getInstance().getModelManager().getMissingModel();
            }
            LegacyBakedModel legacy = new LegacyBakedModel(definition.bakery(), definition.particle());
            BakedModel fallback = original;
            event.getModels().put(baseLocation, new BakedModelWrapper<BakedModel>(fallback) {
                private final ItemOverrides directOverrides = new ItemOverrides() {
                    @Override
                    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                              @Nullable LivingEntity entity, int seed) {
                        return legacy.forItem(stack);
                    }
                };

                @Override
                public ItemOverrides getOverrides() {
                    return directOverrides;
                }
            });
        });
    }

    private record DirectItemBakery(LegacyBakery bakery, Supplier<TextureAtlasSprite> particle) {
    }

    private static void reportMissingItemModels(ModelEvent.ModifyBakingResult event) {
        BakedModel missingModel = Minecraft.getInstance().getModelManager().getMissingModel();
        java.util.SortedSet<String> missingBases = new java.util.TreeSet<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id == null || !id.getNamespace().startsWith("ancientwarfare")) {
                continue;
            }
            BakedModel model = event.getModels().get(new ModelResourceLocation(id, "inventory"));
            if (model == null || model == missingModel) {
                missingBases.add(id.toString());
            }
        }
        java.util.SortedSet<String> missingVariants = new java.util.TreeSet<>();
        for (ResourceLocation location : LegacyModelLoader.ITEM_VARIANTS) {
            BakedModel model = event.getModels().get(location);
            if (model == null || model == missingModel) {
                missingVariants.add(location.toString());
            }
        }
        AncientWarfareCore.LOG.info("AW baked item model audit: missingBases={}, missingVariants={}",
                missingBases.size(), missingVariants.size());
        if (!missingBases.isEmpty()) {
            AncientWarfareCore.LOG.error("AW missing baked base item models: {}", String.join(", ", missingBases));
        }
        if (!missingVariants.isEmpty()) {
            AncientWarfareCore.LOG.error("AW missing baked item variants: {}", String.join(", ", missingVariants));
        }
    }

    private static void installLegacyItemModels(ModelEvent.ModifyBakingResult event) {
        java.util.Set<Item> items = new java.util.HashSet<>(LegacyModelLoader.MESH_DEFINITIONS.keySet());
        LegacyModelLoader.ITEM_MODELS.keySet().forEach(key -> items.add(key.item()));
        for (Item item : items) {
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(item);
            if (itemId == null) {
                continue;
            }
            ModelResourceLocation baseLocation = new ModelResourceLocation(itemId, "inventory");
            BakedModel original = event.getModels().get(baseLocation);
            if (original == null) {
                original = Minecraft.getInstance().getModelManager().getMissingModel();
                AncientWarfareCore.LOG.warn("Dynamic item {} has no base model; legacy variants will still be installed", itemId);
            }
            event.getModels().put(baseLocation, new LegacyItemModel(original, baseLocation, event.getModels()));
        }
    }

    private static final class LegacyItemModel extends BakedModelWrapper<BakedModel> {
        private final ItemOverrides overrides;

        private LegacyItemModel(BakedModel original, ModelResourceLocation baseLocation,
                                Map<ResourceLocation, BakedModel> models) {
            super(original);
            BakedModel missingModel = Minecraft.getInstance().getModelManager().getMissingModel();
            overrides = new ItemOverrides() {
                @Override
                public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                          @Nullable LivingEntity entity, int seed) {
                    ModelResourceLocation location = null;
                    var mesh = LegacyModelLoader.MESH_DEFINITIONS.get(stack.getItem());
                    if (mesh != null) {
                        location = mesh.apply(stack);
                    }
                    if (location == null) {
                        location = LegacyModelLoader.ITEM_MODELS.get(
                                new LegacyModelLoader.ItemModelKey(stack.getItem(), stack.getDamageValue()));
                    }
                    if (location == null || location.equals(baseLocation)) {
                        return model;
                    }
                    BakedModel resolved = models.get(location);
                    if (resolved == null || resolved == missingModel) {
                        return model;
                    }
                    return resolved instanceof LegacyBakedModel legacy ? legacy.forItem(stack) : resolved;
                }
            };
        }

        @Override
        public ItemOverrides getOverrides() {
            return overrides;
        }
    }

    private static void installBakeryModels(ModelEvent.ModifyBakingResult event, Block block, LegacyStateMapperBase mapper) {
        LegacyBakery bakery = LegacyModelBakery.BAKERIES.get(block);
        if (bakery == null) {
            return;
        }
        Map<ModelResourceLocation, LegacyBakedModel> modelsByLegacyLocation = new java.util.HashMap<>();
        block.getStateDefinition().getPossibleStates().forEach(state -> {
            ModelResourceLocation legacyLocation = mapper.map(state);
            LegacyBakedModel model = modelsByLegacyLocation.computeIfAbsent(legacyLocation, location -> {
                BakedModel placeholder = MODELS.get(location);
                return new LegacyBakedModel(bakery, () -> particleOf(event, location, placeholder));
            });
            // The old state mapper name is still needed by legacy items, while the
            // modern BlockModelShaper always requests the canonical property key.
            event.getModels().put(legacyLocation, model);
            event.getModels().put(BlockModelShaper.stateToModelLocation(state), model);
        });
    }

    private static net.minecraft.client.renderer.texture.TextureAtlasSprite particleOf(ModelEvent.ModifyBakingResult event,
                                                                                       ResourceLocation location, BakedModel placeholder) {
        if (placeholder != null) {
            return placeholder.getParticleIcon();
        }
        BakedModel baked = event.getModels().get(location);
        return baked != null ? baked.getParticleIcon() : Minecraft.getInstance().getModelManager().getMissingModel().getParticleIcon();
    }
}
