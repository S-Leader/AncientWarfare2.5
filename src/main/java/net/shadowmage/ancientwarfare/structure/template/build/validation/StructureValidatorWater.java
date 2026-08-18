package net.shadowmage.ancientwarfare.structure.template.build.validation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.worldgen.WorldStructureGenerator;

public class StructureValidatorWater extends StructureValidator {

    public StructureValidatorWater() {
        super(StructureValidationType.WATER);
    }

    @Override
    public boolean shouldIncludeForSelection(Level world, int x, int y, int z, Direction face, StructureTemplate template) {
        int waterSurfaceY = WorldStructureGenerator.getWaterSurfaceY(world, x, z);
        return waterSurfaceY == y - 1
                && world.getBlockState(new BlockPos(x, waterSurfaceY, z)).getFluidState().is(FluidTags.WATER);
    }

    @Override
    public int getAdjustedSpawnY(Level world, int x, int y, int z, Direction face, StructureTemplate template, StructureBB bb) {
        int waterSurfaceY = WorldStructureGenerator.getWaterSurfaceY(world, x, z);
        // StructureBB already applies its historical +1 offset. Supplying the
        // actual water block instead of the air block above it prevents ships
        // and floating buildings from being raised one extra block.
        return waterSurfaceY == y - 1 ? waterSurfaceY : y;
    }

    @Override
    public boolean validatePlacement(Level world, int x, int y, int z, Direction face, StructureTemplate template, StructureBB bb) {
        int minY = getMinY(template, bb);
        return validateBorderBlocks(world, bb, 0, minY, true);
    }

    @Override
    public void preGeneration(Level world, BlockPos pos, Direction face, StructureTemplate template, StructureBB bb) {
        //noop
    }

    @Override
    public void handleClearAction(Level world, BlockPos pos, StructureTemplate template, StructureBB bb) {
        if (pos.getY() < bb.min.getY() + template.getOffset().getY()) {
            world.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        } else {
            super.handleClearAction(world, pos, template, bb);
        }
    }
}
