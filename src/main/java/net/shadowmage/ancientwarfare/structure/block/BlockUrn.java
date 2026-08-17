package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.structure.init.AWStructureSoundTypes;
import net.shadowmage.ancientwarfare.structure.tile.TileUrn;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockUrn extends BlockBaseStructure {
    public BlockUrn() {
        super(LegacyMaterial.CLAY, "urn");
        setHardness(0.4F);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(BlockState state) {
        return false;
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return new AABB(2 / 16D, 0, 2 / 16D, 14 / 16D, 15 / 16D, 14 / 16D);
    }

    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        return AWStructureSoundTypes.URN;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();
    }
}
