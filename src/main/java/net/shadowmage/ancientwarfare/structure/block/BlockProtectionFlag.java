package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelRegistryHelper;
import net.shadowmage.ancientwarfare.core.render.model.LegacyStateMapperBase;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.render.ParticleOnlyModel;
import net.shadowmage.ancientwarfare.structure.render.ParticleSun;
import net.shadowmage.ancientwarfare.structure.render.ProtectionFlagRenderer;
import net.shadowmage.ancientwarfare.structure.tile.TileProtectionFlag;

import javax.annotation.Nullable;

public class BlockProtectionFlag extends BlockFlag {
    public BlockProtectionFlag() {
        super(LegacyMaterial.WOOD, "protection_flag");
        setResistance(6000000F);
        setLightLevel(13 / 15F);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter worldIn, BlockPos pos) {
        float original = super.getDestroyProgress(state, player, worldIn, pos);
        return WorldTools.getTile(worldIn, pos, TileProtectionFlag.class).map(te -> te.getPlayerRelativeBlockHardness(player, original))
                .orElse(original);
    }


    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        WorldTools.getTile(world, pos, TileProtectionFlag.class).ifPresent(te -> te.onActivatedBy(player));
        return true;
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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        int maxParticles = worldIn.getRandom().nextInt(10);
        for (int i = 0; i < maxParticles; i++) {

            double d0 = (double) pos.getX() + worldIn.getRandom().nextFloat();
            double d1 = (double) pos.getY() + 1.9D * worldIn.getRandom().nextFloat();
            double d2 = (double) pos.getZ() + worldIn.getRandom().nextFloat();
            worldIn.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            ParticleSun.spawn(worldIn, d0, d1, d2);
        }
    }
}
