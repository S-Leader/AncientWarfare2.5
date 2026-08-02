package net.shadowmage.ancientwarfare.automation.tile.worksite.treefarm;

import com.google.common.collect.AbstractIterator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.util.BlockTools;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DefaultTreeScanner implements ITreeScanner {
    private final Predicate<BlockState> trunkMatcher;
    private final Set<Predicate<BlockState>> leafMatchers = new HashSet<>();
    private int maxLeafDistance;
    private INextPositionGetter nextPositionGetter;

    private static final int MAX_TRUNK_DISTANCE = 1;

    public Predicate<BlockState> getTrunkMatcher() {
        return trunkMatcher;
    }

    public void addLeafMatcher(Predicate<BlockState> leafMatcher) {
        leafMatchers.add(leafMatcher);
    }

    public DefaultTreeScanner(Predicate<BlockState> trunkMatcher, Predicate<BlockState> leafMatcher) {
        this(trunkMatcher, leafMatcher, CONNECTED_UP_OR_LEVEL, 5);
    }

    public DefaultTreeScanner(Predicate<BlockState> trunkMatcher, Predicate<BlockState> leafMatcher, INextPositionGetter nextPosGetter, int maxLeafDistance) {
        this.trunkMatcher = trunkMatcher;
        leafMatchers.add(leafMatcher);
        this.maxLeafDistance = maxLeafDistance;
        nextPositionGetter = nextPosGetter;
    }

    @Override
    public ITree scanTree(Level world, BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        HorizontalAABB trunkBounds = new HorizontalAABB(pos);

        if (!isTrunk(world.getBlockState(pos))) {
            return Tree.EMPTY;
        }

        List<BlockPos> openList = new ArrayList<>();
        Set<BlockPos> alreadyScanned = new HashSet<>();
        openList.add(pos);
        alreadyScanned.add(pos);

        Tree tree = new Tree(pos);
        while (!openList.isEmpty()) {
            BlockPos current = openList.remove(0);
            Set<BlockPos> toScan = nextPositionGetter.getPositionsToScan(current).filter(p -> !alreadyScanned.contains(p))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            openList.addAll(addTreeBlocks(toScan, world, tree, trunkBounds, minPos, maxPos));
            alreadyScanned.addAll(toScan);
        }

        return tree;
    }

    private Collection<? extends BlockPos> addTreeBlocks(Set<BlockPos> toScan, Level world, Tree tree, HorizontalAABB trunkBounds, BlockPos minPos, BlockPos maxPos) {
        Set<BlockPos> treeBlocks = new HashSet<>();

        for (BlockPos pos : toScan) {
            if (!BlockTools.isPositionWithinHorizontalBounds(pos, minPos, maxPos)) {
                continue;
            }
            BlockState state = world.getBlockState(pos);
            if (isTrunk(state)) {
                tree.addTrunkPosition(pos);
                treeBlocks.add(pos);
                if (trunkBounds.distanceTo(pos) <= MAX_TRUNK_DISTANCE) {
                    trunkBounds.include(pos);
                }
            } else if (isLeaf(state) && trunkBounds.distanceTo(pos) <= maxLeafDistance) {
                tree.addLeafPosition(pos);
                treeBlocks.add(pos);
            }
        }

        return treeBlocks;
    }

    private boolean isLeaf(BlockState state) {
        return leafMatchers.stream().anyMatch(m -> m.test(state));
    }

    private boolean isTrunk(BlockState state) {
        return trunkMatcher.test(state);
    }

    private static Iterable<BlockPos> getPositionsInBoxOrderedByY(BlockPos corner1, BlockPos corner2) {
        return getPositionsInBoxOrderedByY(Math.min(corner1.getX(), corner2.getX()), Math.min(corner1.getY(), corner2.getY()),
                Math.min(corner1.getZ(), corner2.getZ()), Math.max(corner1.getX(), corner2.getX()), Math.max(corner1.getY(), corner2.getY()),
                Math.max(corner1.getZ(), corner2.getZ()));
    }

    private static Iterable<BlockPos> getPositionsInBoxOrderedByY(int x1, int y1, int z1, int x2, int y2, int z2) {
        return () -> new AbstractIterator<BlockPos>() {
            private boolean first = true;
            private int lastPosX;
            private int lastPosY;
            private int lastPosZ;

            protected BlockPos computeNext() {
                if (this.first) {
                    this.first = false;
                    this.lastPosX = x1;
                    this.lastPosY = y1;
                    this.lastPosZ = z1;
                    return new BlockPos(x1, y1, z1);
                } else if (this.lastPosX == x2 && this.lastPosY == y2 && this.lastPosZ == z2) {
                    return this.endOfData();
                } else {
                    if (this.lastPosX < x2) {
                        ++this.lastPosX;
                    } else if (this.lastPosZ < z2) {
                        this.lastPosX = x1;
                        ++this.lastPosZ;
                    } else if (this.lastPosY < y2) {
                        this.lastPosX = x1;
                        this.lastPosZ = z1;
                        ++this.lastPosY;
                    }

                    return new BlockPos(this.lastPosX, this.lastPosY, this.lastPosZ);
                }
            }
        };
    }

    @Override
    public boolean matches(BlockState state) {
        return trunkMatcher.test(state);
    }

    public interface INextPositionGetter {
        Stream<BlockPos> getPositionsToScan(BlockPos currentPos);
    }

    public static final INextPositionGetter ALL_AROUND = currentPos -> {
        Iterable<BlockPos> blocksInBox = getPositionsInBoxOrderedByY(currentPos.offset(-1, -1, -1), currentPos.offset(1, 1, 1));
        return StreamSupport.stream(blocksInBox.spliterator(), false);
    };

    public static final INextPositionGetter CONNECTED_AROUND = currentPos -> Arrays.stream(Direction.values()).map(currentPos::relative);

    public static final INextPositionGetter CONNECTED_UP_OR_LEVEL = new INextPositionGetter() {
        private final Direction[] offsets = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP};

        @Override
        public Stream<BlockPos> getPositionsToScan(BlockPos currentPos) {
            return Arrays.stream(offsets).map(currentPos::relative);
        }
    };

    public static final INextPositionGetter CONNECTED_DOWN_OR_LEVEL = new INextPositionGetter() {
        private final Direction[] offsets = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN};

        @Override
        public Stream<BlockPos> getPositionsToScan(BlockPos currentPos) {
            return Arrays.stream(offsets).map(currentPos::relative);
        }
    };

    public static final INextPositionGetter ALL_UP_OR_LEVEL = currentPos -> {
        Iterable<BlockPos> blocksInBox = getPositionsInBoxOrderedByY(currentPos.offset(-1, 0, -1), currentPos.offset(1, 1, 1));
        return StreamSupport.stream(blocksInBox.spliterator(), false);
    };
}
