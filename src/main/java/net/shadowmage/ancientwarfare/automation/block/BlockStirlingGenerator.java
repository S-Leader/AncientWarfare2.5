package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.gui.GuiStirlingGenerator;
import net.shadowmage.ancientwarfare.automation.render.StirlingGeneratorRenderer;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileStirlingGenerator;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.render.model.*;

public class BlockStirlingGenerator extends BlockTorqueGenerator implements LegacyBakeryProvider {

    public BlockStirlingGenerator(String regName) {
        super(regName);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyModelState getLegacyModelState(BlockState state, BlockGetter world, BlockPos pos) {
        return StirlingGeneratorRenderer.INSTANCE.handleState(LegacyModelState.of(state), world, pos);
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyBakery getBakery() {
        return StirlingGeneratorRenderer.INSTANCE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();
        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return StirlingGeneratorRenderer.MODEL_LOCATION;
            }
        });

        LegacyModelRegistryHelper.register(StirlingGeneratorRenderer.MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return StirlingGeneratorRenderer.INSTANCE.sprite;
            }
        });
    }
}
