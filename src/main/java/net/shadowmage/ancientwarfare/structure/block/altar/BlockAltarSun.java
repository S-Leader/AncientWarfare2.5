package net.shadowmage.ancientwarfare.structure.block.altar;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.structure.render.ParticleSun;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class BlockAltarSun extends BlockAltarTop {
    private static final AABB AABB_NORTH_SOUTH = new AABB(1 / 16D, 0, 5 / 16D, 15 / 16D, 1D, 11 / 16D);
    private static final AABB AABB_WEST_EAST = new AABB(5 / 16D, 0, 1 / 16D, 11 / 16D, 1D, 15 / 16D);

    public BlockAltarSun() {
        super(LegacyMaterial.WOOD, "altar_sun");
        setLightLevel(12 / 15F);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(FACING, Direction.from2DDataValue(meta));
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return state.getValue(FACING).get2DDataValue();
    }

    @Override
    public BlockState getStateForPlacement(Level worldIn, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
        return defaultBlockState().setValue(FACING, placer.getDirection().getOpposite());
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case WEST:
            case EAST:
                return AABB_WEST_EAST;
            case NORTH:
            case SOUTH:
            default:
                return AABB_NORTH_SOUTH;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean canRenderInLayer(BlockState state, RenderType layer) {
        return layer == RenderType.cutout();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
        int maxParticles = world.getRandom().nextInt(3);
        for (int i = 0; i < maxParticles; i++) {
            double angle = world.getRandom().nextFloat() * Math.PI * 2d;
            float distance = world.getRandom().nextFloat() * 5 / 16f;
            double sunHorizontalOffset = 0.5d + Math.sin(angle) * distance;
            double distanceFromSun = 0.5d + (world.getRandom().nextBoolean() ? 0.1d : -0.1d);
            double yOffset = 0.55d + Math.cos(angle) * distance;
            Direction.Axis facingAxis = state.getValue(FACING).getAxis();
            double xOffset = facingAxis == Direction.Axis.X ? distanceFromSun : sunHorizontalOffset;
            double zOffset = facingAxis == Direction.Axis.Z ? distanceFromSun : sunHorizontalOffset;
            ParticleSun.spawn(world, pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset);
        }
    }
}
