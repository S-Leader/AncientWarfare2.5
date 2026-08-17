package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.render.WaterwheelGeneratorRenderer;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileWaterwheelGenerator;
import net.shadowmage.ancientwarfare.core.render.BlockStateKeyGenerator;
import net.shadowmage.ancientwarfare.core.render.model.*;
import net.shadowmage.ancientwarfare.core.render.property.CoreProperties;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;

public class BlockWaterwheelGenerator extends BlockTorqueGenerator implements LegacyBakeryProvider {
    public static final BooleanProperty VALID_SETUP = BooleanProperty.create("valid_setup");

    public BlockWaterwheelGenerator(String regName) {
        super(regName);
    }

    @Override
    protected void addProperties(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(VALID_SETUP);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyModelState getLegacyModelState(BlockState state, BlockGetter world, BlockPos pos) {
        return WaterwheelGeneratorRenderer.INSTANCE.handleState(LegacyModelState.of(state), world, pos);
    }

    @Override
    public boolean invertFacing() {
        return true;
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        ModelLoaderHelper.registerItem(this, WaterwheelGeneratorRenderer.MODEL_LOCATION);

        LegacyModelBakery.registerBlockKeyGenerator(this, new BlockStateKeyGenerator.Builder().addKeyProperties(VALID_SETUP, CoreProperties.UNLISTED_FACING).addKeyProperties(AutomationProperties.DYNAMIC).addKeyProperties(o -> String.format("%.6f", o), AutomationProperties.ROTATIONS).build());

        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return WaterwheelGeneratorRenderer.MODEL_LOCATION;
            }
        });

        LegacyModelRegistryHelper.register(WaterwheelGeneratorRenderer.MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return WaterwheelGeneratorRenderer.INSTANCE.sprite;
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyBakery getBakery() {
        return WaterwheelGeneratorRenderer.INSTANCE;
    }
}
