package net.shadowmage.ancientwarfare.structure.template.build.validation.border;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.template.build.validation.border.points.PointType;
import net.shadowmage.ancientwarfare.structure.template.build.validation.border.points.SmoothingPoint;
import net.shadowmage.ancientwarfare.structure.worldgen.WorldStructureGenerator;

import java.util.*;
import java.util.function.Consumer;

public class SmoothingMatrix {
    private SmoothingPoint[][] smoothingPoints;
    private final int fullXSize;
    private final int fullZSize;
    private Map<PointType, Set<SmoothingPoint>> typePoints = new HashMap<>();
    private final BlockPos minPos;

    public SmoothingMatrix(BorderMatrix borderMatrix, BlockPos structureMinPos, int borderSize) {
        fullXSize = borderMatrix.getFullXSize();
        fullZSize = borderMatrix.getFullZSize();
        smoothingPoints = initMatrix(fullXSize, fullZSize);
        minPos = structureMinPos.offset(-borderSize - 2, 0, -borderSize - 2);
    }

    Optional<SmoothingPoint> getPoint(HorizontalCoords coords) {
        return getPoint(coords.getX(), coords.getZ());
    }

    public Optional<SmoothingPoint> getPoint(int x, int z) {
        if (smoothingPoints.length == 0 || x < 0 || x >= smoothingPoints.length || z < 0 || z >= smoothingPoints[0].length) {
            return Optional.empty();
        }

        return Optional.ofNullable(smoothingPoints[x][z]);
    }

    private void printMatrix() {
        printTypes();
        printDistances();
        printHeights();
    }

    private void printHeights() {
        for (int x = 0; x < fullXSize; x++) {
            for (int z = 0; z < fullZSize; z++) {
                System.out.print(String.format("%02d", getPoint(x, z).map(point -> !point.hasSmoothedPosSet() ? 0 : point.getSmoothedPos().getY()).orElse(0)) + " ");
            }
            System.out.println();
        }
    }

    private void printDistances() {
        for (int x = 0; x < fullXSize; x++) {
            for (int z = 0; z < fullZSize; z++) {
                System.out.print(getPoint(x, z).map(point -> point.getStructureBorderDistance() == Integer.MAX_VALUE ? "00"
                        : String.format("%02d", point.getStructureBorderDistance())).orElse("00") + " ");
            }
            System.out.println();
        }
    }

    private void printTypes() {
        for (int x = 0; x < fullXSize; x++) {
            for (int z = 0; z < fullZSize; z++) {
                System.out.print(getPoint(x, z).map(point -> point.getType().getAcronym()).orElse(" ") + " ");
            }
            System.out.println();
        }
    }

    public boolean isEmpty(HorizontalCoords point) {
        return !getPoint(point).isPresent();
    }

    public SmoothingPoint addPoint(int x, int z, BlockPos pos, PointType type) {
        SmoothingPoint point = new SmoothingPoint(x, z, pos, type);
        addPoint(point);
        return point;
    }

    public SmoothingPoint addPoint(int x, int z, BlockPos pos, PointType type, BlockState state) {
        SmoothingPoint point = addPoint(x, z, pos, type);
        point.setBlockState(state);
        return point;
    }

    void addPoint(SmoothingPoint point) {
        smoothingPoints[point.getX()][point.getZ()] = point;
        addTypePoint(point.getType(), point);
    }

    private SmoothingPoint[][] initMatrix(int fullXSize, int fullZSize) {
        SmoothingPoint[][] ret = new SmoothingPoint[fullXSize][];

        for (int x = 0; x < fullXSize; x++) {
            ret[x] = new SmoothingPoint[fullZSize];
        }

        return ret;
    }

    private void addTypePoint(PointType type, SmoothingPoint point) {
        if (!typePoints.containsKey(type)) {
            typePoints.put(type, new HashSet<>());
        }
        typePoints.get(type).add(point);
    }

    public void apply(Level world, Consumer<BlockPos> handleClearing) {
        typePoints.get(PointType.SMOOTHED_BORDER).forEach(point -> {
            levelTerrain(world, point, handleClearing);
        });
        // Chunks are already decorated by modern world generation. Re-running the
        // biome's PlacedFeatures here is both destructive and illegal in 1.20:
        // BiomeFilter requires a registered top-level feature context. The thrown
        // exception used to abort the build after only its dirt smoothing border
        // had been placed, leaving the hollow frames seen in-game.
        typePoints.get(PointType.SMOOTHED_BORDER).forEach(point -> {
            BlockPos posAbove = point.getSmoothedPos().above();
            if (world.getBiome(posAbove).value().shouldSnow(world, posAbove)) {
                world.setBlock(posAbove, Blocks.SNOW.defaultBlockState(), 2);
            }
        });
    }

    private void levelTerrain(Level world, SmoothingPoint point, Consumer<BlockPos> handleClearing) {
        BlockPos originalPos = point.getWorldPos();
        BlockPos smoothedPos = point.getSmoothedPos();

        int topNonWaterY = WorldStructureGenerator.getTargetY(world, originalPos.getX(), originalPos.getZ(), true, originalPos.getY());
        int topOuterBorderWaterY = point.getOuterBorderPoint().getWaterLevel();

        if (originalPos.getY() == smoothedPos.getY() && topNonWaterY == originalPos.getY()) {
            return;
        }

        if (originalPos.getY() > smoothedPos.getY()) {
            if (smoothedPos.getY() < topOuterBorderWaterY) {
                BlockTools.getAllInBoxTopDown(smoothedPos, new BlockPos(smoothedPos.getX(), topOuterBorderWaterY, smoothedPos.getZ()))
                        .forEach(pos -> world.setBlock(pos, Blocks.WATER.defaultBlockState(), 3));
            } else {
                BlockTools.getAllInBoxTopDown(smoothedPos, originalPos).forEach(handleClearing);
            }
        }
        if (smoothedPos.getY() - topNonWaterY > 1) {
            BlockState fillerBlock = getBiomeFillerBlockState(world, originalPos.getX(), originalPos.getZ(), topNonWaterY);
            BlockPos.betweenClosed(originalPos.getX(), topNonWaterY + 1, originalPos.getZ(), originalPos.getX(), smoothedPos.getY() - 1, originalPos.getZ())
                    .forEach(pos -> world.setBlock(pos.immutable(), fillerBlock, 3));
        }
        world.setBlock(smoothedPos, point.getBlockState(), 3);
    }

    private BlockState getBiomeFillerBlockState(Level world, int x, int z, int topY) {
        // Modern biomes no longer expose the fillerBlock field - sample the column below the surface instead.
        BlockState filler = world.getBlockState(new BlockPos(x, topY - 1, z));
        return filler.isAir() ? Blocks.DIRT.defaultBlockState() : filler;
    }

    public BlockPos getMinPos() {
        return minPos;
    }

    public Set<SmoothingPoint> getPointsOfType(PointType type) {
        return typePoints.get(type);
    }
}
