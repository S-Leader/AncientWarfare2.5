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
import net.shadowmage.ancientwarfare.automation.render.HandCrankedGeneratorRenderer;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileHandCrankedGenerator;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RotationType;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.render.model.*;

public class BlockHandCrankedGenerator extends BlockTorqueBase implements LegacyBakeryProvider {
    public BlockHandCrankedGenerator(String regName) {
        super(LegacyMaterial.ROCK, regName);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyModelState getLegacyModelState(BlockState state, BlockGetter world, BlockPos pos) {
        return HandCrankedGeneratorRenderer.INSTANCE.handleState(LegacyModelState.of(state), world, pos);
    }


    @Override
    public RotationType getRotationType() {
        return RotationType.FOUR_WAY;
    }

    @Override
    public boolean invertFacing() {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();

        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return HandCrankedGeneratorRenderer.MODEL_LOCATION;
            }
        });

        LegacyModelRegistryHelper.register(HandCrankedGeneratorRenderer.MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return HandCrankedGeneratorRenderer.INSTANCE.sprite;
            }
        });
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyBakery getBakery() {
        return HandCrankedGeneratorRenderer.INSTANCE;
    }
}
