package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;

import static net.shadowmage.ancientwarfare.core.render.property.CoreProperties.FACING;

public class BlockTotemCube extends BlockBaseStructure {
    private boolean isLit;

    public BlockTotemCube(String regName, boolean isLit) {
        super(LegacyMaterial.ROCK, regName);
        this.isLit = isLit;
        if (isLit) {
            setLightLevel(0.875F);
        }
    }

    public BlockTotemCube(String regName) {
        this(regName, false);
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
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand) {
        if (isLit) {
            double d0 = (double) pos.getX() + 0.5D;
            double d1 = (double) pos.getY() + rand.nextDouble() * 6.0D / 16.0D + 5 / 16D;
            double d2 = (double) pos.getZ() + 0.5D;
            double d4 = rand.nextDouble() * 0.6D - 0.3D;

            if (rand.nextDouble() < 0.1D) {
                worldIn.playLocalSound((double) pos.getX() + 0.5D, (double) pos.getY(), (double) pos.getZ() + 0.5D, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            Direction enumfacing = stateIn.getValue(FACING);
            spawnParticles(worldIn, d0, d1, d2, d4, enumfacing);
            spawnParticles(worldIn, d0, d1, d2, d4, enumfacing.getOpposite());
        }
    }

    private void spawnParticles(Level worldIn, double x, double y, double z, double d4, Direction enumfacing) {
        switch (enumfacing) {
            case WEST:
                worldIn.addParticle(ParticleTypes.SMOKE, x - 0.52D, y, z + d4, 0.0D, 0.0D, 0.0D);
                worldIn.addParticle(ParticleTypes.FLAME, x - 0.52D, y, z + d4, 0.0D, 0.0D, 0.0D);
                break;
            case EAST:
                worldIn.addParticle(ParticleTypes.SMOKE, x + 0.52D, y, z + d4, 0.0D, 0.0D, 0.0D);
                worldIn.addParticle(ParticleTypes.FLAME, x + 0.52D, y, z + d4, 0.0D, 0.0D, 0.0D);
                break;
            case NORTH:
                worldIn.addParticle(ParticleTypes.SMOKE, x + d4, y, z - 0.52D, 0.0D, 0.0D, 0.0D);
                worldIn.addParticle(ParticleTypes.FLAME, x + d4, y, z - 0.52D, 0.0D, 0.0D, 0.0D);
                break;
            case SOUTH:
            default:
                worldIn.addParticle(ParticleTypes.SMOKE, x + d4, y, z + 0.52D, 0.0D, 0.0D, 0.0D);
                worldIn.addParticle(ParticleTypes.FLAME, x + d4, y, z + 0.52D, 0.0D, 0.0D, 0.0D);
        }
    }
}
