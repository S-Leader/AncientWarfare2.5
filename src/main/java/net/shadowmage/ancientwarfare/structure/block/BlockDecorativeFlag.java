package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelRegistryHelper;
import net.shadowmage.ancientwarfare.core.render.model.LegacyStateMapperBase;
import net.shadowmage.ancientwarfare.structure.render.ParticleOnlyModel;
import net.shadowmage.ancientwarfare.structure.render.ProtectionFlagRenderer;
import net.shadowmage.ancientwarfare.structure.tile.TileDecorativeFlag;

import javax.annotation.Nullable;

public class BlockDecorativeFlag extends BlockFlag {
    public BlockDecorativeFlag() {
        super(LegacyMaterial.WOOD, "decorative_flag");
        setResistance(5.0F);
        setHardness(2.0F);
    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        //noinspection ConstantConditions
        ModelResourceLocation modelLocation = new ModelResourceLocation(getRegistryName(), "normal");
        LegacyModelRegistryHelper.registerItemRenderer(this.asItem(), new ProtectionFlagRenderer());
        LegacyModelRegistryHelper.register(modelLocation, ParticleOnlyModel.INSTANCE);
        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return modelLocation;
            }
        });

    }
}
