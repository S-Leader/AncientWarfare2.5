package net.shadowmage.ancientwarfare.structure.template.build.validation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.worldgen.WorldStructureGenerator;

import static net.shadowmage.ancientwarfare.structure.template.build.validation.properties.StructureValidationProperties.*;

public class StructureValidatorUnderground extends StructureValidator {
    public StructureValidatorUnderground() {
        super(StructureValidationType.UNDERGROUND);
    }

    @Override
    public boolean shouldIncludeForSelection(Level world, int x, int y, int z, Direction face, StructureTemplate template) {
        int tHeight = (template.getSize().getY() - template.getOffset().getY());
        int low = getMinGenerationDepth() + tHeight + getMinOverfill();
        return WorldStructureGenerator.getTargetY(world, x, z, true) > low;
    }

    @Override
    public int getAdjustedSpawnY(Level world, int x, int y, int z, Direction face, StructureTemplate template, StructureBB bb) {
        int range = getMaxGenerationDepth() - getMinGenerationDepth() + 1;
        int tHeight = (template.getSize().getY() - template.getOffset().getY());
        return WorldStructureGenerator.getTargetY(world, x, z, true) - getMinOverfill() - world.random.nextInt(range) - tHeight;
    }

    private int getMaxGenerationDepth() {
        return getPropertyValue(MAX_GENERATION_DEPTH);
    }

    private int getMinGenerationDepth() {
        return getPropertyValue(MIN_GENERATION_DEPTH);
    }

    @Override
    public boolean validatePlacement(Level world, int x, int y, int z, Direction face, StructureTemplate template, StructureBB bb) {
        int minY = bb.min.getY() + template.getOffset().getY() + getMinOverfill();
        int topBlockY;
        for (int bx = bb.min.getX(); bx <= bb.max.getX(); bx++) {
            for (int bz = bb.min.getZ(); bz <= bb.max.getZ(); bz++) {
                topBlockY = WorldStructureGenerator.getTargetY(world, bx, bz, true);
                if (topBlockY <= minY) {
                    return false;
                }
            }
        }
        return true;
    }

    private int getMinOverfill() {
        return getPropertyValue(MIN_OVERFILL);
    }

    @Override
    public void preGeneration(Level world, BlockPos pos, Direction face, StructureTemplate template, StructureBB bb) {
        //noop
    }

}
