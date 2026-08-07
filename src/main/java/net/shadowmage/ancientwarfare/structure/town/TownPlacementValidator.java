package net.shadowmage.ancientwarfare.structure.town;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.config.AWStructureStatics;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureEntry;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureMap;
import net.shadowmage.ancientwarfare.structure.gamedata.TownMap;
import net.shadowmage.ancientwarfare.structure.worldgen.WorldStructureGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class TownPlacementValidator {
    private TownPlacementValidator() {
    }

    private static final int ABSOLUTE_MAX_SIZE = 32;
    private static final int MIN_STRUCTURE_DISTANCE = TownGeneratorBorders.MAX_BORDER_WIDTH + 5;

    /*
     * input a single X, Y, Z coordinate to examine the nearby area for potential town generation.<br>
     * First examines the chunk that was at the input coordinates, and expands out from there attempting
     * to create the largest town-bounding area that it can.
     *
     * @param x world x to search from
     * @param z world z to search from
     * @return maximal bounding area for a town, or null if no acceptable area was found starting in the specified chunk
     */
    static Optional<TownBoundingArea> findGenerationPosition(Level world, int x, int z, int requestedMaxSize, int requiredMinSize) {
        int maxSize = Math.max(1, Math.min(ABSOLUTE_MAX_SIZE, requestedMaxSize));
        int minimumRequiredSize = Math.max(1, Math.min(maxSize, requiredMinSize));
        if (isTownClose(world, x, z)) {
            return Optional.empty();
        }

        int cx = x >> 4;
        int cz = z >> 4;

        int height = getTopFilledHeight(world.getChunk(cx, cz), x, z);
        if (height <= 0) {
            return Optional.empty();
        }

        int minY = Math.max(world.getMinBuildHeight(), world.getSeaLevel() - 4);

        TownBoundingArea area = new TownBoundingArea();
        area.minY = Math.max(minY, height - 3);
        area.maxY = Math.min(world.getMaxBuildHeight() - 1, area.minY + 40);
        area.chunkMinX = cx;
        area.chunkMaxX = cx;
        area.chunkMinZ = cz;
        area.chunkMaxZ = cz;

        StructureMap sm = AWGameData.INSTANCE.getPerWorldData(world, StructureMap.class);
        Collection<StructureEntry> structureList = sm.getEntriesNear(world, x, z, maxSize + 2, true, new ArrayList<>());

        if (isStructureInside(structureList, x, z, area.minY, area.maxY)) {
            return Optional.empty();
        }

        expandBoundingArea(world, area, structureList, maxSize);
        shrinkTooLongArea(area);
        if (Math.min(area.getChunkWidth(), area.getChunkLength()) < minimumRequiredSize) {
            return Optional.empty();
        }
        levelToAverageBorderHeight(world, area);
        return Optional.of(area);
    }

    private static final int STEP = 4;

    private static void levelToAverageBorderHeight(Level world, TownBoundingArea area) {
        // Modern terrain has isolated holes, pools and sharp decoration that skew a
        // plain arithmetic average. Use the handoff port's representative median
        // idea while keeping the original AW2 rectangular expansion algorithm.
        int minX = area.getBlockMinX() - 1;
        int maxX = area.getBlockMaxX() + 1;
        int minZ = area.getBlockMinZ() - 1;
        int maxZ = area.getBlockMaxZ() + 1;
        ArrayList<Integer> samples = new ArrayList<>();

        for (int x = minX + STEP; x < maxX; x += STEP) {
            samples.add(WorldStructureGenerator.getTargetY(world, x, minZ, false));
            samples.add(WorldStructureGenerator.getTargetY(world, x, maxZ, false));
        }
        for (int z = minZ + STEP; z < maxZ; z += STEP) {
            samples.add(WorldStructureGenerator.getTargetY(world, minX, z, false));
            samples.add(WorldStructureGenerator.getTargetY(world, maxX, z, false));
        }
        samples.removeIf(y -> y < world.getMinBuildHeight());
        if (!samples.isEmpty()) {
            samples.sort(Integer::compareTo);
            area.setSurfaceY(samples.get(samples.size() / 2));
        }
    }

    private static void shrinkTooLongArea(TownBoundingArea area) {
        int cw = area.getChunkWidth();
        int cl = area.getChunkLength();
        if (cw > cl * 2) {
            int diff = cw - (cl * 2);
            while (diff > 0) {
                area.chunkMaxX--;
                diff--;
                if (diff > 0) {
                    area.chunkMinX++;
                    diff--;
                }
            }
        }
        if (cl > cw * 2) {
            int diff = cl - (cw * 2);
            while (diff > 0) {
                area.chunkMaxZ--;
                diff--;
                if (diff > 0) {
                    area.chunkMinZ++;
                    diff--;
                }
            }
        }
    }

    private static boolean isStructureInside(Collection<StructureEntry> structureList, int x, int z, int minY, int maxY) {
        for (StructureEntry structure : structureList) {
            if (structure.getBB().intersects(x - MIN_STRUCTURE_DISTANCE, minY, z - MIN_STRUCTURE_DISTANCE,
                    x + 16 + MIN_STRUCTURE_DISTANCE, maxY, z + 16 + MIN_STRUCTURE_DISTANCE)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTownClose(Level world, int x, int z) {
        TownMap tm = AWGameData.INSTANCE.getPerWorldData(world, TownMap.class);
        int minDist = AWStructureStatics.townClosestDistance;
        float dist = tm.getClosestTown(x, z, minDist * 2);
        return dist < minDist;
    }

    private static void expandBoundingArea(Level world, TownBoundingArea area, Collection<StructureEntry> structureList, int maxSize) {
        boolean xneg = true;//if should try and expand on this direction next pass, once set to false it never checks that direction again
        boolean xpos = true;
        boolean zneg = true;
        boolean zpos = true;
        boolean didExpand = false;//set to true if any expansion occurred on that pass.  if false at end of pass, will break out of loop as no more expansion is possible
        do {
            didExpand = false;
            if (xneg && area.getChunkWidth() < maxSize) {
                xneg = tryExpandXNeg(world, area, structureList);
                didExpand = xneg;
            }
            if (xpos && area.getChunkWidth() < maxSize) {
                xpos = tryExpandXPos(world, area, structureList);
                didExpand = didExpand || xpos;
            }
            if (zneg && area.getChunkLength() < maxSize) {
                zneg = tryExpandZNeg(world, area, structureList);
                didExpand = didExpand || zneg;
            }
            if (zpos && area.getChunkLength() < maxSize) {
                zpos = tryExpandZPos(world, area, structureList);
                didExpand = didExpand || zpos;
            }
        } while (didExpand && (area.getChunkWidth() < maxSize || area.getChunkLength() < maxSize));
    }

    private static boolean tryExpandXNeg(Level world, TownBoundingArea area, Collection<StructureEntry> structureList) {
        int cx = area.chunkMinX - 1;
        for (int z = area.chunkMinZ; z <= area.chunkMaxZ; z++) {
            if (!isAverageHeightWithin(world, cx, z, area.minY, area.maxY) || isStructureInside(structureList, cx << 4, z << 4, area.minY, area.maxY)) {
                return false;
            }
        }
        area.chunkMinX = cx;
        return true;
    }

    private static boolean tryExpandXPos(Level world, TownBoundingArea area, Collection<StructureEntry> structureList) {
        int cx = area.chunkMaxX + 1;
        for (int z = area.chunkMinZ; z <= area.chunkMaxZ; z++) {
            if (!isAverageHeightWithin(world, cx, z, area.minY, area.maxY) || isStructureInside(structureList, cx << 4, z << 4, area.minY, area.maxY)) {
                return false;
            }
        }
        area.chunkMaxX = cx;
        return true;
    }

    private static boolean tryExpandZNeg(Level world, TownBoundingArea area, Collection<StructureEntry> structureList) {
        int cz = area.chunkMinZ - 1;
        for (int x = area.chunkMinX; x <= area.chunkMaxX; x++) {
            if (!isAverageHeightWithin(world, x, cz, area.minY, area.maxY) || isStructureInside(structureList, x << 4, cz << 4, area.minY, area.maxY)) {
                return false;
            }
        }
        area.chunkMinZ = cz;
        return true;
    }

    private static boolean tryExpandZPos(Level world, TownBoundingArea area, Collection<StructureEntry> structureList) {
        int cz = area.chunkMaxZ + 1;
        for (int x = area.chunkMinX; x <= area.chunkMaxX; x++) {
            if (!isAverageHeightWithin(world, x, cz, area.minY, area.maxY) || isStructureInside(structureList, x << 4, cz << 4, area.minY, area.maxY)) {
                return false;
            }
        }
        area.chunkMaxZ = cz;
        return true;
    }

    private static boolean isAverageHeightWithin(Level world, int cx, int cz, int min, int max) {
        ArrayList<Integer> samples = new ArrayList<>(64);
        int startX = cx << 4;
        int startZ = cz << 4;
        for (int x = startX; x < startX + 16; x += 2) {
            for (int z = startZ; z < startZ + 16; z += 2) {
                int y = WorldStructureGenerator.getTargetY(world, x, z, false);
                if (y < world.getMinBuildHeight()) {
                    return false;
                }
                samples.add(y);
            }
        }
        samples.sort(Integer::compareTo);
        int low = samples.get((int) Math.round((samples.size() - 1) * 0.10D));
        int median = samples.get(samples.size() / 2);
        int high = samples.get((int) Math.round((samples.size() - 1) * 0.90D));

        // Ignore isolated pits/peaks, but still reject genuinely steep chunks.
        return median >= min && median <= max && high - low <= 18;
    }

    /*
     * return the highest Y that has a solid non-skipped block in it<br>
     * This implementation skips water, air, and any blocks on the world-gen skippable blocks list (trees, plants, etc)
     *
     * @return top solid block height, or -1 for invalid top block or no top block found (void, bedrock...)
     */
    private static int getTopFilledHeight(LevelChunk chunk, int x, int z) {
        int maxY = chunk.getHighestSectionPosition() + 15;
        Block block;
        int minY = chunk.getMinBuildHeight();
        for (int y = maxY; y >= minY; y--) {
            BlockState state = chunk.getBlockState(new BlockPos(x, y, z));
            block = state.getBlock();
            if (AWStructureStatics.isSkippable(state)) {
                continue;
            }
            if (LegacyMaterial.of(state).isLiquid()) {
                if (y >= 56) {
                    continue;
                }// >=56 is fillable through underfill/border settings.  below that is too deep for a proper gradient on the border.
                return -1;//return invalid Y if liquid block is too low
            }
            if (!AWStructureStatics.isValidTargetBlock(state)) {
                AncientWarfareStructure.LOG.debug("rejecting town chunk for non-target block: {} :: {}:{}", block, chunk.getPos().x, chunk.getPos().z);
                return -1;
            }
            return y;//if not skippable and is valid target block, return that y-level
        }
        return -1;
    }
}
