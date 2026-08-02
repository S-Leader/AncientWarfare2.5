package net.shadowmage.ancientwarfare.structure.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelRegistryHelper;
import net.shadowmage.ancientwarfare.core.render.model.LegacyStateMapperBase;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.item.ItemBlockStoneCoffin;
import net.shadowmage.ancientwarfare.structure.render.ParticleOnlyModel;
import net.shadowmage.ancientwarfare.structure.render.StoneCoffinRenderer;
import net.shadowmage.ancientwarfare.structure.tile.TileStoneCoffin;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class BlockStoneCoffin extends BlockCoffin<TileStoneCoffin> {
    private static final EnumProperty<Variant> VARIANT = EnumProperty.create("variant", Variant.class);

    private static final AABB AABB = new AABB(0, 0D, 0D, 1D, 14.1 / 16D, 1D);

    public BlockStoneCoffin() {
        super(LegacyMaterial.ROCK, "stone_coffin", TileStoneCoffin::new, TileStoneCoffin.class);
    }

    public void getSubBlocks(CreativeModeTab itemIn, NonNullList<ItemStack> items) {
        for (Variant variant : Variant.values()) {
            items.add(ItemBlockStoneCoffin.getVariantStack(variant));
        }
    }

    @Override
    protected List<Property> getAdditionalProperties() {
        return Collections.singletonList(VARIANT);
    }

    @Override
    protected void setPlacementProperties(Level world, BlockPos pos, LivingEntity placer, ItemStack stack, TileStoneCoffin te) {
        te.setVariant(ItemBlockStoneCoffin.getVariant(stack));
    }

    @Override
    protected ItemStack getVariantStack(IVariant variant) {
        return ItemBlockStoneCoffin.getVariantStack(variant);
    }

    @Override
    protected IVariant getDefaultVariant() {
        return Variant.getDefault();
    }

    @Override
    public BlockState getActualState(BlockState state, BlockGetter world, BlockPos pos) {
        return state.setValue(VARIANT, WorldTools.getTile(world, pos, TileStoneCoffin.class).map(TileStoneCoffin::getVariant).orElse(Variant.STONE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        Map<Variant, ModelResourceLocation> variantModels = new EnumMap<>(Variant.class);
        for (Variant variant : Variant.values()) {
            ModelResourceLocation modelLocation = new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID, "structure/wooden_coffin"), variant.getName());
            variantModels.put(variant, modelLocation);
            LegacyModelRegistryHelper.register(modelLocation, new ParticleOnlyModel(variant.getBlockTexture()));
        }

        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return variantModels.get(state.getValue(VARIANT));
            }
        });
        LegacyModelRegistryHelper.registerItemRenderer(this.asItem(), new StoneCoffinRenderer());
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return AABB;
    }

    public enum Variant implements IVariant {
        STONE("stone", "stone"),
        SANDSTONE("sandstone", "sandstone_top"),
        PRISMARINE("prismarine", "prismarine_bricks"),
        DEMONIC("demonic", "nether_brick");

        private String name;
        private String blockTexture;

        Variant(String name, String blockTexture) {
            this.name = name;
            this.blockTexture = blockTexture;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public String getName() {
            return name;
        }

        public static Variant getDefault() {
            return STONE;
        }

        private static final ImmutableMap<String, Variant> NAME_TO_VARIANT;

        static {
            ImmutableMap.Builder<String, Variant> builder = new ImmutableMap.Builder<>();
            for (Variant variant : values()) {
                builder.put(variant.name, variant);
            }
            NAME_TO_VARIANT = builder.build();
        }

        public static Variant fromName(String name) {
            return NAME_TO_VARIANT.getOrDefault(name, STONE);
        }

        public String getBlockTexture() {
            return blockTexture;
        }
    }
}
