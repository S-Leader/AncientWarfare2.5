package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.render.TorqueJunctionRenderer;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileConduitHeavy;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileConduitLight;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileConduitMedium;
import net.shadowmage.ancientwarfare.core.render.BlockStateKeyGenerator;
import net.shadowmage.ancientwarfare.core.render.model.*;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;

public class BlockTorqueJunction extends BlockTorqueTransportSided implements LegacyBakeryProvider {
    public BlockTorqueJunction(String regName) {
        super(regName);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyModelState getLegacyModelState(BlockState state, BlockGetter world, BlockPos pos) {
        return TorqueJunctionRenderer.INSTANCE.handleState(LegacyModelState.of(state), world, pos);
    }


    /**
     * The junction uses a CodeChickenLib-generated dynamic model. Rendering the
     * vanilla breaking overlay for that model makes Rubidium rebuild the CCL
     * quads while the block is being damaged, which can terminate the client
     * before Java has a chance to write a crash report. Render the complete model
     * through the block-entity renderer instead.
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity createTileEntity(Level world, BlockState state) {
        switch (state.getValue(AutomationProperties.TIER)) {
            case LIGHT:
                return new TileConduitLight();
            case MEDIUM:
                return new TileConduitMedium();
            case HEAVY:
                return new TileConduitHeavy();
        }
        return new TileConduitLight();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        ModelLoaderHelper.registerItem(this, "automation", "light", false); //the actual switch for itemstack types is processed by renderer

        LegacyModelBakery.registerBlockKeyGenerator(this, new BlockStateKeyGenerator.Builder().addKeyProperties(AutomationProperties.TIER, CoreProperties.UNLISTED_FACING).addKeyProperties(AutomationProperties.DYNAMIC).addKeyProperties(BlockTorqueTransportSided.CONNECTIONS).addKeyProperties(o -> String.format("%.6f", o), AutomationProperties.ROTATIONS).build());

        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                switch (state.getValue(AutomationProperties.TIER)) {
                    case LIGHT:
                        return TorqueJunctionRenderer.LIGHT_MODEL_LOCATION;
                    case MEDIUM:
                        return TorqueJunctionRenderer.MEDIUM_MODEL_LOCATION;
                    default:
                        return TorqueJunctionRenderer.HEAVY_MODEL_LOCATION;
                }
            }
        });

        LegacyModelRegistryHelper.register(TorqueJunctionRenderer.LIGHT_MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return TorqueJunctionRenderer.INSTANCE.getSprite(TorqueTier.LIGHT);
            }
        });

        LegacyModelRegistryHelper.register(TorqueJunctionRenderer.MEDIUM_MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return TorqueJunctionRenderer.INSTANCE.getSprite(TorqueTier.MEDIUM);
            }
        });

        LegacyModelRegistryHelper.register(TorqueJunctionRenderer.HEAVY_MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return TorqueJunctionRenderer.INSTANCE.getSprite(TorqueTier.HEAVY);
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyBakery getBakery() {
        return TorqueJunctionRenderer.INSTANCE;
    }
}
