package net.shadowmage.ancientwarfare.structure.template.build.validation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;

import static net.shadowmage.ancientwarfare.structure.template.build.validation.properties.StructureValidationProperties.*;

public class StructureValidatorSky extends StructureValidator {
    public StructureValidatorSky() {
        super(StructureValidationType.SKY);
    }

    @Override
    protected void setDefaultSettings(StructureTemplate template) {
        //noop
    }

    @Override
    public boolean shouldIncludeForSelection(Level world, int x, int y, int z, Direction face, StructureTemplate template) {
        int remainingHeight = world.getMaxBuildHeight() - getMinFlyingHeight() - (template.getSize().getY() - template.getOffset().getY());
        return y < remainingHeight;
    }

    @Override
    public int getAdjustedSpawnY(Level world, int x, int y, int z, Direction face, StructureTemplate template, StructureBB bb) {
        int range = getMaxGenerationHeight() - getMinGenerationHeight() + 1;
        return y + getMinFlyingHeight() + world.random.nextInt(range);
    }

    @Override
    public boolean validatePlacement(Level world, int x, int y, int z, Direction face, StructureTemplate template, StructureBB bb) {
        int maxY = getMinGenerationHeight() - getMinFlyingHeight();
        return validateBorderBlocks(world, bb, 0, maxY, false);
    }

    @Override
    public void preGeneration(Level world, BlockPos pos, Direction face, StructureTemplate template, StructureBB bb) {
        //noop
    }

    private int getMinFlyingHeight() {
        return getPropertyValue(MIN_FLYING_HEIGHT);
    }

    private int getMaxGenerationHeight() {
        return getPropertyValue(MAX_GENERATION_HEIGHT);
    }

    private int getMinGenerationHeight() {
        return getPropertyValue(MIN_GENERATION_HEIGHT);
    }
}
