package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.render.FlywheelControllerRenderer;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileFlywheelControllerHeavy;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileFlywheelControllerLight;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileFlywheelControllerMedium;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RotationType;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.render.BlockStateKeyGenerator;
import net.shadowmage.ancientwarfare.core.render.model.*;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;
import net.shadowmage.ancientwarfare.core.util.LegacyItemStack;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;

public class BlockFlywheelController extends BlockTorqueBase implements LegacyBakeryProvider {
    public static final LegacyModelProperty<Float> FLYWHEEL_ROTATION = LegacyModelProperty.create("flywheel_rotation");

    public BlockFlywheelController(String regName) {
        super(LegacyMaterial.ROCK, regName);
    }

    @Override
    protected void addProperties(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(AutomationProperties.TIER);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(AutomationProperties.TIER, TorqueTier.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(AutomationProperties.TIER).getMeta();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyModelState getLegacyModelState(BlockState state, BlockGetter world, BlockPos pos) {
        return FlywheelControllerRenderer.INSTANCE.handleState(LegacyModelState.of(state), world, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return getStateFromMeta(context.getItemInHand().getDamageValue());
    }

    @Override
    public boolean isSideSolid(BlockState base_state, BlockGetter world, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public BlockEntity createTileEntity(Level world, BlockState state) {
        switch (state.getValue(AutomationProperties.TIER)) {
            case LIGHT:
                return new TileFlywheelControllerLight();
            case MEDIUM:
                return new TileFlywheelControllerMedium();
            case HEAVY:
                return new TileFlywheelControllerHeavy();
        }
        return null;
    }

    public void getSubBlocks(CreativeModeTab item, NonNullList<ItemStack> items) {
        items.add(LegacyItemStack.of(this, 1, 0));
        items.add(LegacyItemStack.of(this, 1, 1));
        items.add(LegacyItemStack.of(this, 1, 2));
    }

    @Override
    public boolean invertFacing() {
        return false;
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        ModelLoaderHelper.registerItem(this, "automation", "light", false); //the actual switch for itemstack types is processed by renderer

        LegacyModelBakery.registerBlockKeyGenerator(this, new BlockStateKeyGenerator.Builder().addKeyProperties(AutomationProperties.TIER, CoreProperties.UNLISTED_FACING).addKeyProperties(AutomationProperties.DYNAMIC, FLYWHEEL_ROTATION, AutomationProperties.USE_INPUT).addKeyProperties(o -> String.format("%.6f", o), AutomationProperties.ROTATIONS).addKeyProperties(o -> String.format("%.6f", o), AutomationProperties.INPUT_ROTATION).build());

        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                switch (state.getValue(AutomationProperties.TIER)) {
                    case LIGHT:
                        return FlywheelControllerRenderer.LIGHT_MODEL_LOCATION;
                    case MEDIUM:
                        return FlywheelControllerRenderer.MEDIUM_MODEL_LOCATION;
                    default:
                        return FlywheelControllerRenderer.HEAVY_MODEL_LOCATION;
                }
            }
        });

        LegacyModelRegistryHelper.register(FlywheelControllerRenderer.LIGHT_MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return FlywheelControllerRenderer.INSTANCE.getSprite(TorqueTier.LIGHT);
            }
        });

        LegacyModelRegistryHelper.register(FlywheelControllerRenderer.MEDIUM_MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return FlywheelControllerRenderer.INSTANCE.getSprite(TorqueTier.MEDIUM);
            }
        });

        LegacyModelRegistryHelper.register(FlywheelControllerRenderer.HEAVY_MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return FlywheelControllerRenderer.INSTANCE.getSprite(TorqueTier.HEAVY);
            }
        });

    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyBakery getBakery() {
        return FlywheelControllerRenderer.INSTANCE;
    }
}
