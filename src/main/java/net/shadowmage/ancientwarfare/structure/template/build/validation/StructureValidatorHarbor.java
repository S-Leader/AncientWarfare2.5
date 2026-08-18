package net.shadowmage.ancientwarfare.structure.template.build.validation;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.worldgen.WorldStructureGenerator;

import java.util.Set;

public class StructureValidatorHarbor extends StructureValidator {
    private Set<Block> validTargetBlocks = ImmutableSet.of(
            Blocks.DIRT,
            Blocks.GRASS_BLOCK,
            Blocks.STONE,
            Blocks.SAND,
            Blocks.GRAVEL,
            Blocks.SANDSTONE,
            Blocks.CLAY,
            Blocks.IRON_ORE,
            Blocks.COAL_ORE
    );

    public StructureValidatorHarbor() {
        super(StructureValidationType.HARBOR);
    }

    @Override
    protected void setDefaultSettings(StructureTemplate template) {
        //noop
    }

    @Override
    public boolean shouldIncludeForSelection(Level world, int x, int y, int z, Direction face, StructureTemplate template) {
        /*
         * testing that front target position is valid block
         * then test back target position to ensure that it has water at same level
         * or at an acceptable level difference
         */
        Block block = world.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
        if (validTargetBlocks.contains(block)) {
            BlockPos testPosition = new BlockPos(x, y, z).relative(face, template.getOffset().getZ());
            int by = WorldStructureGenerator.getTargetY(world, testPosition.getX(), testPosition.getZ(), false);
            if (y - by > getMaxFill()) {
                return false;
            }
            block = world.getBlockState(new BlockPos(testPosition.getX(), by, testPosition.getZ())).getBlock();
            if (block == Blocks.WATER) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getAdjustedSpawnY(Level world, int x, int y, int z, Direction face, StructureTemplate template, StructureBB bb) {
        BlockPos testPosition = new BlockPos(x, y, z).relative(face, template.getOffset().getZ());
        return WorldStructureGenerator.getTargetY(world, testPosition.getX(), testPosition.getZ(), false) + 1;
    }

    @Override
    public boolean validatePlacement(Level world, int x, int y, int z, Direction face, StructureTemplate template, StructureBB bb) {
        int minY = getMinY(template, bb);
        int maxY = getMaxY(template, bb);
        StructureBB edge = bb.getFrontCorners(face);
        for (int bx = edge.min.getX(); bx <= edge.max.getX(); bx++) {
            for (int bz = edge.min.getZ(); bz <= edge.max.getZ(); bz++) {
                if (!validateBlockHeightTypeAndBiome(world, bx, bz, minY, maxY, false)) {
                    return false;
                }
            }
        }

        edge = bb.getRearCorners(face);
        for (int bx = edge.min.getX(); bx <= edge.max.getX(); bx++) {
            for (int bz = edge.min.getZ(); bz <= edge.max.getZ(); bz++) {
                if (!validateBlockHeightAndType(world, bx, bz, minY, maxY, false, state -> LegacyMaterial.of(state) == LegacyMaterial.WATER)) {
                    return false;
                }
            }
        }

        edge = bb.getRightCorners(face);
        for (int bx = edge.min.getX(); bx <= edge.max.getX(); bx++) {
            for (int bz = edge.min.getZ(); bz <= edge.max.getZ(); bz++) {
                if (!validateBlockHeightAndType(world, bx, bz, minY, maxY, false,
                        state -> AWStructureStatics.isValidTargetBlock(state) || LegacyMaterial.of(state) == LegacyMaterial.WATER)) {
                    return false;
                }
            }
        }

        edge = bb.getLeftCorners(face);
        for (int bx = edge.min.getX(); bx <= edge.max.getX(); bx++) {
            for (int bz = edge.min.getZ(); bz <= edge.max.getZ(); bz++) {
                if (!validateBlockHeightAndType(world, bx, bz, minY, maxY, false,
                        state -> AWStructureStatics.isValidTargetBlock(state) || LegacyMaterial.of(state) == LegacyMaterial.WATER)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void preGeneration(Level world, BlockPos pos, Direction face, StructureTemplate template, StructureBB bb) {
        prePlacementBorder(world, template, bb);
    }

    @Override
    public void handleClearAction(Level world, BlockPos pos, StructureTemplate template, StructureBB bb) {
        if (pos.getY() >= bb.min.getY() + template.getOffset().getY()) {
            super.handleClearAction(world, pos, template, bb);
        } else {
            world.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
        }
    }

}
