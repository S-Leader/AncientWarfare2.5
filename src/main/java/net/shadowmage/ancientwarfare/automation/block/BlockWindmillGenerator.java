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
import net.shadowmage.ancientwarfare.automation.render.WindmillGeneratorRenderer;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileWindmillController;
import net.shadowmage.ancientwarfare.core.render.model.*;

public class BlockWindmillGenerator extends BlockTorqueGenerator implements LegacyBakeryProvider {

    public BlockWindmillGenerator(String regName) {
        super(regName);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyModelState getLegacyModelState(BlockState state, BlockGetter world, BlockPos pos) {
        return WindmillGeneratorRenderer.INSTANCE.handleState(LegacyModelState.of(state), world, pos);
    }

    @Override
    public boolean invertFacing() {
        return true;
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public LegacyBakery getBakery() {
        return WindmillGeneratorRenderer.INSTANCE;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();

        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return WindmillGeneratorRenderer.MODEL_LOCATION;
            }
        });

        LegacyModelRegistryHelper.register(WindmillGeneratorRenderer.MODEL_LOCATION, new LegacyBakeryModel() {
            @Override
            @OnlyIn(Dist.CLIENT)
            public TextureAtlasSprite getParticleTexture() {
                return WindmillGeneratorRenderer.INSTANCE.sprite;
            }
        });
    }

}
