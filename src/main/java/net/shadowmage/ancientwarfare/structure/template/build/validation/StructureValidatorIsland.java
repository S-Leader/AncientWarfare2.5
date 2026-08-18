package net.shadowmage.ancientwarfare.structure.template.build.validation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.worldgen.WorldStructureGenerator;

import static net.shadowmage.ancientwarfare.structure.template.build.validation.properties.StructureValidationProperties.MAX_WATER_DEPTH;
import static net.shadowmage.ancientwarfare.structure.template.build.validation.properties.StructureValidationProperties.MIN_WATER_DEPTH;

public class StructureValidatorIsland extends StructureValidator {
    public StructureValidatorIsland() {
        super(StructureValidationType.ISLAND);
    }

    private int getMinWaterDepth() {
        return getPropertyValue(MIN_WATER_DEPTH);
    }

    private int getMaxWaterDepth() {
        return getPropertyValue(MAX_WATER_DEPTH);
    }

    private void setMinWaterDepth(int depth) {
        setPropertyValue(MIN_WATER_DEPTH, depth);
    }

    private void setMaxWaterDepth(int depth) {
        setPropertyValue(MAX_WATER_DEPTH, depth);
    }

    @Override
    protected void setDefaultSettings(StructureTemplate template) {
        setMaxWaterDepth(template.getOffset().getY());
        setMinWaterDepth(getMaxWaterDepth() / 2);
    }

    @Override
    public boolean shouldIncludeForSelection(Level world, int x, int y, int z, Direction face, StructureTemplate template) {
        int startY = y - 1;
        y = WorldStructureGenerator.getTargetY(world, x, z, true) + 1;
        int water = startY - y + 1;
        return !(water < getMinWaterDepth() || water > getMaxWaterDepth());
    }

    @Override
    public int getAdjustedSpawnY(Level world, int x, int y, int z, Direction face, StructureTemplate template, StructureBB bb) {
        int waterSurfaceY = WorldStructureGenerator.getWaterSurfaceY(world, x, z);
        // As with WATER validation, StructureBB performs the historical +1.
        // Anchor to the water block itself so islands do not protrude one block
        // above the surrounding ocean.
        return waterSurfaceY == y - 1 ? waterSurfaceY : y;
    }

    @Override
    public boolean validatePlacement(Level world, int x, int y, int z, Direction face, StructureTemplate template, StructureBB bb) {
        int minY = y - getMaxWaterDepth();
        int maxY = y - getMinWaterDepth();
        return validateBorderBlocks(world, bb, minY, maxY, true);
    }

    @Override
    public void preGeneration(Level world, BlockPos pos, Direction face, StructureTemplate template, StructureBB bb) {
        prePlacementUnderfill(world, bb);
    }

    @Override
    public void handleClearAction(Level world, BlockPos pos, StructureTemplate template, StructureBB bb) {
        int maxWaterY = bb.min.getY() + template.getOffset().getY() - 1;
        if (pos.getY() <= maxWaterY) {
            world.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        } else {
            super.handleClearAction(world, pos, template, bb);
        }
    }

}
