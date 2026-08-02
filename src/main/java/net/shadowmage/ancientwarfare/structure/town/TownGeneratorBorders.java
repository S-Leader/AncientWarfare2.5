package net.shadowmage.ancientwarfare.structure.town;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.automation.registry.TreeFarmRegistry;
import net.shadowmage.ancientwarfare.automation.tile.worksite.treefarm.ITree;
import net.shadowmage.ancientwarfare.automation.tile.worksite.treefarm.ITreeScanner;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.template.build.validation.border.SmoothingMatrixBuilder;

import java.util.Optional;

public class TownGeneratorBorders {
    private static final int CLEAR_TREE_MAX_BORDER_DISTANCE = 10;
    public static final int MAX_BORDER_WIDTH = 20;

    private TownGeneratorBorders() {
    }

    public static void generateBorders(Level world, StructureBB exterior) {
        BlockTools.getAllInBoxTopDown(exterior.min, exterior.max.offset(0, 50, 0)).forEach(pos -> handleClearing(world, pos));

        new SmoothingMatrixBuilder(world, exterior, Math.min(Math.max(exterior.getXSize(), exterior.getZSize()) / 8, MAX_BORDER_WIDTH),
                exterior.min.getY() - 1, p -> getFillBlock(world, p.getX(), p.getZ(), true)).build()
                .apply(world, pos -> handleClearing(world, pos));
    }

    public static void levelTownArea(Level world, StructureBB bb) {
        int minX = bb.min.getX();
        int minZ = bb.min.getZ();
        int maxX = bb.max.getX();
        int maxZ = bb.max.getZ();
        int desiredTopBlockHeight = bb.min.getY() - 1;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                handleBorderBlock(world, x, z, desiredTopBlockHeight, desiredTopBlockHeight, getFillBlock(world, x, z, false), getFillBlock(world, x, z, true));
                world.setBlock(new BlockPos(x, desiredTopBlockHeight - 5, z), Blocks.COBBLESTONE.defaultBlockState(), 3);
            }
        }
    }

    private static void handleBorderBlock(Level world, int x, int z, int fillLevel, int cutLevel, BlockState fillBlock, BlockState topBlock) {
        int y = BlockTools.getTopFilledHeight(world.getChunkAt(new BlockPos(x, 1, z)), x, z, false);
        int topSolidY = BlockTools.getTopFilledHeight(world.getChunkAt(new BlockPos(x, 1, z)), x, z, true);
        if (y >= cutLevel) {
            for (int py = y; py > Math.min(topSolidY, cutLevel); py--) {
                BlockPos clearPos = new BlockPos(x, py, z);
                handleClearing(world, clearPos);
            }
            if (topSolidY > cutLevel) {
                world.setBlock(new BlockPos(x, cutLevel, z), topBlock, 3);
            }
        }
        if (topSolidY <= fillLevel) {
            for (int py = topSolidY + 1; py < fillLevel; py++) {
                world.setBlock(new BlockPos(x, py, z), fillBlock, 3);
            }
            world.setBlock(new BlockPos(x, fillLevel, z), topBlock, 3);
        }
    }

    private static void handleClearing(Level world, BlockPos clearPos) {
        BlockState state = world.getBlockState(clearPos);
        if (LegacyMaterial.of(state) != LegacyMaterial.AIR) {
            Optional<ITreeScanner> treeScanner = TreeFarmRegistry.getRegisteredTreeScanner(state);
            if (!treeScanner.isPresent()) {
                world.removeBlock(clearPos, false);
                return;
            }
            ITree tree = treeScanner.get().scanTree(world, clearPos,
                    clearPos.offset(-CLEAR_TREE_MAX_BORDER_DISTANCE, 0, -CLEAR_TREE_MAX_BORDER_DISTANCE),
                    clearPos.offset(CLEAR_TREE_MAX_BORDER_DISTANCE, 0, CLEAR_TREE_MAX_BORDER_DISTANCE));
            tree.getLeafPositions().forEach(pos -> world.removeBlock(pos, false));
            tree.getTrunkPositions().forEach(pos -> world.removeBlock(pos, false));
        }
    }

    private static BlockState getFillBlock(Level world, int x, int z, boolean surface) {
        // Modern biomes no longer expose mutable topBlock/fillerBlock fields.
        // Sample the actual generated column so modded surface materials are kept.
        int topY = BlockTools.getTopFilledHeight(world, x, z, true);
        BlockPos topPos = new BlockPos(x, topY, z);
        BlockState top = world.getBlockState(topPos);
        if (surface) {
            return top.isAir() ? Blocks.GRASS_BLOCK.defaultBlockState() : top;
        }
        BlockState filler = world.getBlockState(topPos.below());
        return filler.isAir() ? Blocks.DIRT.defaultBlockState() : filler;
    }
}
