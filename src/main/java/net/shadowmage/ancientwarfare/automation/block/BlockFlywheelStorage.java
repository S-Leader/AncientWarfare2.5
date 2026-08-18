package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.init.AWAutomationBlocks;
import net.shadowmage.ancientwarfare.automation.render.FlywheelStorageRenderer;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.multiblock.TileFlywheelStorage;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.render.BlockStateKeyGenerator;
import net.shadowmage.ancientwarfare.core.render.model.*;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import java.util.Optional;

import static net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties.*;

public class BlockFlywheelStorage extends BlockBaseAutomation implements LegacyBakeryProvider {
    private final TorqueTier fixedTier;

    public BlockFlywheelStorage(String regName) {
        this(regName, null);
    }

    public BlockFlywheelStorage(String regName, TorqueTier fixedTier) {
        super(LegacyMaterial.ROCK.properties().noOcclusion(), regName);
        this.fixedTier = fixedTier;
        if (fixedTier != null) {
            registerDefaultState(defaultBlockState().setValue(TIER, fixedTier));
        }
    }

    public TorqueTier getFixedTier() {
        return fixedTier;
    }

    @Override
    public boolean eventReceived(BlockState state, Level world, BlockPos pos, int id, int param) {
        return WorldTools.sendClientEventToTile(world, pos, id, param);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(TIER);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(TIER,
                fixedTier == null ? TorqueTier.byMetadata(meta) : fixedTier);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return fixedTier == null ? state.getValue(TIER).getMeta() : 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return fixedTier == null
                ? getStateFromMeta(context.getItemInHand().getDamageValue())
                : defaultBlockState().setValue(TIER, fixedTier);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyModelState getLegacyModelState(BlockState state, BlockGetter world, BlockPos pos) {
        return FlywheelStorageRenderer.INSTANCE.handleState(LegacyModelState.of(state), world, pos);
    }

    @Override
    public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        WorldTools.getTile(world, pos, TileFlywheelStorage.class).ifPresent(TileFlywheelStorage::blockPlaced);
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public void breakBlock(Level world, BlockPos pos, BlockState state) {
        Optional<TileFlywheelStorage> te = WorldTools.getTile(world, pos, TileFlywheelStorage.class);
        super.breakBlock(world, pos, state);
        te.ifPresent(TileFlywheelStorage::blockBroken); //have to call post block-break so that the controller properly sees the block as gone
    }

    @Override
    public int damageDropped(BlockState state) {
        return fixedTier == null ? state.getValue(TIER).getMeta() : 0;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter level, BlockPos pos, BlockState state, int fortune) {
        Item item = fixedTier == null
                ? AWAutomationBlocks.getFlywheelStorageItem(state.getValue(TIER))
                : asItem();
        if (item != null) {
            drops.add(new ItemStack(item));
        }
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    public void getSubBlocks(CreativeModeTab creativeTab, NonNullList<ItemStack> list) {
        list.add(new ItemStack(this));
    }

    @Override
    public boolean canRenderInLayer(BlockState state, RenderType layer) {
        return layer == RenderType.solid() || layer == RenderType.translucent();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        LegacyModelRegistryHelper.registerDirectItemBakery(this.asItem(), FlywheelStorageRenderer.INSTANCE,
                () -> FlywheelStorageRenderer.INSTANCE.getSprite(fixedTier == null ? TorqueTier.LIGHT : fixedTier));
        ModelLoaderHelper.registerItem(this, fixedTier == null
                ? FlywheelStorageRenderer.LIGHT_MODEL_LOCATION
                : modelLocation(fixedTier));

        LegacyModelBakery.registerBlockKeyGenerator(this, new BlockStateKeyGenerator.Builder().addKeyProperties(TIER).addKeyProperties(DYNAMIC, IS_CONTROL, WIDTH, HEIGHT).addKeyProperties(o -> String.format("%.6f", o), ROTATION).build());

        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                switch (state.getValue(AutomationProperties.TIER)) {
                    case LIGHT:
                        return FlywheelStorageRenderer.LIGHT_MODEL_LOCATION;
                    case MEDIUM:
                        return FlywheelStorageRenderer.MEDIUM_MODEL_LOCATION;
                    default:
                        return FlywheelStorageRenderer.HEAVY_MODEL_LOCATION;
                }
            }
        });

        LegacyModelRegistryHelper.register(FlywheelStorageRenderer.LIGHT_MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return FlywheelStorageRenderer.INSTANCE.getSprite(TorqueTier.LIGHT);
            }
        });

        LegacyModelRegistryHelper.register(FlywheelStorageRenderer.MEDIUM_MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return FlywheelStorageRenderer.INSTANCE.getSprite(TorqueTier.MEDIUM);
            }
        });

        LegacyModelRegistryHelper.register(FlywheelStorageRenderer.HEAVY_MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return FlywheelStorageRenderer.INSTANCE.getSprite(TorqueTier.HEAVY);
            }
        });

    }

    private static ModelResourceLocation modelLocation(TorqueTier tier) {
        return switch (tier) {
            case LIGHT -> FlywheelStorageRenderer.LIGHT_MODEL_LOCATION;
            case MEDIUM -> FlywheelStorageRenderer.MEDIUM_MODEL_LOCATION;
            case HEAVY -> FlywheelStorageRenderer.HEAVY_MODEL_LOCATION;
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyBakery getBakery() {
        return FlywheelStorageRenderer.INSTANCE;
    }
}
