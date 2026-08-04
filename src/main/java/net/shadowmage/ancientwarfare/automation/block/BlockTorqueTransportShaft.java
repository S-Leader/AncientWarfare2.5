package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.init.AWAutomationBlocks;
import net.shadowmage.ancientwarfare.automation.render.TorqueShaftRenderer;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueShaft;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueShaftHeavy;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueShaftLight;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueShaftMedium;
import net.shadowmage.ancientwarfare.core.render.BlockStateKeyGenerator;
import net.shadowmage.ancientwarfare.core.render.model.*;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import java.util.Optional;

public class BlockTorqueTransportShaft extends BlockTorqueTransport implements LegacyBakeryProvider {
    public static final LegacyModelProperty<Boolean> HAS_PREVIOUS = LegacyModelProperty.create("has_previous", false);
    public static final LegacyModelProperty<Boolean> HAS_NEXT = LegacyModelProperty.create("has_next", false);
    private static final AABB CENTER_BOX = new AABB(0.1875D, 0.1875D, 0.1875D, 0.8125D, 0.8125D, 0.8125D);
    private static final AABB X_AXIS_BOX = new AABB(0D, 0.1875D, 0.1875D, 1D, 0.8125D, 0.8125D);
    private static final AABB Y_AXIS_BOX = new AABB(0.1875D, 0D, 0.1875D, 0.8125D, 1D, 0.8125D);
    private static final AABB Z_AXIS_BOX = new AABB(0.1875D, 0.1875D, 0D, 0.8125D, 0.8125D, 1D);

    public BlockTorqueTransportShaft(String regName) {
        super(regName);
    }

    public BlockTorqueTransportShaft(String regName, TorqueTier fixedTier) {
        super(regName, fixedTier);
    }

    @Override
    protected Item getVariantItem(TorqueTier tier) {
        return AWAutomationBlocks.getTorqueShaftItem(tier);
    }

    @Override
    public BlockEntity createTileEntity(Level world, BlockState state) {
        switch (getTier(state)) {
            case LIGHT:
                return new TileTorqueShaftLight();
            case MEDIUM:
                return new TileTorqueShaftMedium();
            case HEAVY:
                return new TileTorqueShaftHeavy();
        }
        return new TileTorqueShaftLight();
    }

    @Override
    protected void addProperties(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.addProperties(builder);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyModelState getLegacyModelState(BlockState state, BlockGetter world, BlockPos pos) {
        return TorqueShaftRenderer.INSTANCE.handleState(LegacyModelState.of(state), world, pos);
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter world, BlockPos pos) {
        Optional<TileTorqueShaft> te = WorldTools.getTile(world, pos, TileTorqueShaft.class);
        if (te.isPresent()) {
            return switch (te.get().getPrimaryFacing().getAxis()) {
                case X -> X_AXIS_BOX;
                case Y -> Y_AXIS_BOX;
                case Z -> Z_AXIS_BOX;
            };
        }
        return CENTER_BOX;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        if (getFixedTier() == null) {
            ModelLoaderHelper.registerItem(this, "automation", "light", false);
        } else {
            ModelLoaderHelper.registerItem(this, modelLocation(getFixedTier()));
        }

        LegacyModelBakery.registerBlockKeyGenerator(this, new BlockStateKeyGenerator.Builder().addKeyProperties(AutomationProperties.TIER, CoreProperties.UNLISTED_FACING).addKeyProperties(AutomationProperties.DYNAMIC, HAS_PREVIOUS, HAS_NEXT).addKeyProperties(o -> String.format("%.6f", o), AutomationProperties.INPUT_ROTATION).addKeyProperties(o -> String.format("%.6f", o), AutomationProperties.ROTATIONS).build());

        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                switch (getTier(state)) {
                    case LIGHT:
                        return TorqueShaftRenderer.LIGHT_MODEL_LOCATION;
                    case MEDIUM:
                        return TorqueShaftRenderer.MEDIUM_MODEL_LOCATION;
                    default:
                        return TorqueShaftRenderer.HEAVY_MODEL_LOCATION;
                }
            }
        });

        LegacyModelRegistryHelper.register(TorqueShaftRenderer.LIGHT_MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return TorqueShaftRenderer.INSTANCE.getSprite(TorqueTier.LIGHT);
            }
        });

        LegacyModelRegistryHelper.register(TorqueShaftRenderer.MEDIUM_MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return TorqueShaftRenderer.INSTANCE.getSprite(TorqueTier.MEDIUM);
            }
        });

        LegacyModelRegistryHelper.register(TorqueShaftRenderer.HEAVY_MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return TorqueShaftRenderer.INSTANCE.getSprite(TorqueTier.HEAVY);
            }
        });
    }

    private static ModelResourceLocation modelLocation(TorqueTier tier) {
        return switch (tier) {
            case LIGHT -> TorqueShaftRenderer.LIGHT_MODEL_LOCATION;
            case MEDIUM -> TorqueShaftRenderer.MEDIUM_MODEL_LOCATION;
            case HEAVY -> TorqueShaftRenderer.HEAVY_MODEL_LOCATION;
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyBakery getBakery() {
        return TorqueShaftRenderer.INSTANCE;
    }
}
