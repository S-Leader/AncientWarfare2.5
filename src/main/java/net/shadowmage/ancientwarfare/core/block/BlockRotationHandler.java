package net.shadowmage.ancientwarfare.core.block;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public class BlockRotationHandler {
    public static Direction getFaceForPlacement(LivingEntity entity, IRotatableBlock block, Direction sideHit) {
        if (block.getRotationType() == RotationType.NONE) {
            return Direction.NORTH;
        }
        Direction facing = entity.getDirection();
        if (block.getRotationType() == RotationType.SIX_WAY && sideHit.getAxis() == Direction.Axis.Y) {
            facing = sideHit.getOpposite();
        }
        if (block.invertFacing()) {
            facing = facing.getOpposite();
        }
        return facing;
    }

    public interface IRotatableBlock {
        RotationType getRotationType();

        boolean invertFacing();
    }

    public interface IRotatableTile {
        Direction getPrimaryFacing();

        void setPrimaryFacing(Direction face);
    }

    public enum RotationType {
        /*
         * Can have 6 textures / inventories.<br>
         * Top, Bottom, Front, Rear, Left, Right<br>
         * Can only face in one of four-directions - N/S/E/W
         */
        FOUR_WAY(EnumSet.of(RelativeSide.TOP, RelativeSide.BOTTOM, RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.FRONT, RelativeSide.REAR)), /*
         * Can have 3 textures / inventories<br>
         * Top, Bottom, Sides<br>
         * Can face in any orientation - U/D/N/S/E/W
         */
        SIX_WAY(EnumSet.of(RelativeSide.TOP, RelativeSide.BOTTOM, RelativeSide.ANY_SIDE)), /*
         * No rotation, can still have relative sides, but FRONT always == NORTH
         */
        NONE(EnumSet.of(RelativeSide.TOP, RelativeSide.BOTTOM, RelativeSide.LEFT, RelativeSide.RIGHT, RelativeSide.FRONT, RelativeSide.REAR));

        RotationType(EnumSet<RelativeSide> sides) {
            validSides = sides;
        }

        EnumSet<RelativeSide> validSides;

        public Set<RelativeSide> getValidSides() {
            return validSides;
        }
    }

    public enum RelativeSide {
        TOP("guistrings.inventory.side.top"), BOTTOM("guistrings.inventory.side.bottom"), FRONT("guistrings.inventory.side.front"), REAR("guistrings.inventory.side.rear"), LEFT("guistrings.inventory.side.left"), RIGHT("guistrings.inventory.side.right"), ANY_SIDE("guistrings.inventory.side.all_sides"), NONE("guistrings.inventory.side.none");

        private static final int DOWN = Direction.DOWN.ordinal();
        private static final int UP = Direction.UP.ordinal();
        private static final int NORTH = Direction.NORTH.ordinal();
        private static final int SOUTH = Direction.SOUTH.ordinal();
        private static final int WEST = Direction.WEST.ordinal();
        private static final int EAST = Direction.EAST.ordinal();
        //[side-viewed][block-facing]=relative side viewed
        protected static final RelativeSide[][] sixWayMap = new RelativeSide[6][6];
        //[side-viewed][block-facing]=relative side viewed
        protected static final RelativeSide[][] fourWayMap = new RelativeSide[6][6];

        static {
            //D,U,N,S,W,E
            //[side-viewed][block-facing]=relative side viewed
            //fourWayMap[X][0-1] SHOULD BE NEVER REFERENCED AS BLOCK CAN NEVER POINT U/D
            sixWayMap[DOWN][DOWN] = TOP;
            sixWayMap[DOWN][UP] = BOTTOM;
            sixWayMap[DOWN][NORTH] = ANY_SIDE;
            sixWayMap[DOWN][SOUTH] = ANY_SIDE;
            sixWayMap[DOWN][WEST] = ANY_SIDE;
            sixWayMap[DOWN][EAST] = ANY_SIDE;

            sixWayMap[UP][DOWN] = BOTTOM;
            sixWayMap[UP][UP] = TOP;
            sixWayMap[UP][NORTH] = ANY_SIDE;
            sixWayMap[UP][SOUTH] = ANY_SIDE;
            sixWayMap[UP][WEST] = ANY_SIDE;
            sixWayMap[UP][EAST] = ANY_SIDE;

            sixWayMap[NORTH][DOWN] = ANY_SIDE;
            sixWayMap[NORTH][UP] = ANY_SIDE;
            sixWayMap[NORTH][NORTH] = TOP;
            sixWayMap[NORTH][SOUTH] = BOTTOM;
            sixWayMap[NORTH][WEST] = ANY_SIDE;
            sixWayMap[NORTH][EAST] = ANY_SIDE;

            sixWayMap[SOUTH][DOWN] = ANY_SIDE;
            sixWayMap[SOUTH][UP] = ANY_SIDE;
            sixWayMap[SOUTH][NORTH] = BOTTOM;
            sixWayMap[SOUTH][SOUTH] = TOP;
            sixWayMap[SOUTH][WEST] = ANY_SIDE;
            sixWayMap[SOUTH][EAST] = ANY_SIDE;

            sixWayMap[WEST][DOWN] = ANY_SIDE;
            sixWayMap[WEST][UP] = ANY_SIDE;
            sixWayMap[WEST][NORTH] = ANY_SIDE;
            sixWayMap[WEST][SOUTH] = ANY_SIDE;
            sixWayMap[WEST][WEST] = TOP;
            sixWayMap[WEST][EAST] = BOTTOM;

            sixWayMap[EAST][DOWN] = ANY_SIDE;
            sixWayMap[EAST][UP] = ANY_SIDE;
            sixWayMap[EAST][NORTH] = ANY_SIDE;
            sixWayMap[EAST][SOUTH] = ANY_SIDE;
            sixWayMap[EAST][WEST] = BOTTOM;
            sixWayMap[EAST][EAST] = TOP;

            fourWayMap[DOWN][DOWN] = ANY_SIDE;
            fourWayMap[DOWN][UP] = ANY_SIDE;
            fourWayMap[DOWN][NORTH] = BOTTOM;
            fourWayMap[DOWN][SOUTH] = BOTTOM;
            fourWayMap[DOWN][WEST] = BOTTOM;
            fourWayMap[DOWN][EAST] = BOTTOM;

            fourWayMap[UP][DOWN] = ANY_SIDE;
            fourWayMap[UP][UP] = ANY_SIDE;
            fourWayMap[UP][NORTH] = TOP;
            fourWayMap[UP][SOUTH] = TOP;
            fourWayMap[UP][WEST] = TOP;
            fourWayMap[UP][EAST] = TOP;

            fourWayMap[NORTH][DOWN] = ANY_SIDE;
            fourWayMap[NORTH][UP] = ANY_SIDE;
            fourWayMap[NORTH][NORTH] = FRONT;
            fourWayMap[NORTH][SOUTH] = REAR;
            fourWayMap[NORTH][WEST] = RIGHT;
            fourWayMap[NORTH][EAST] = LEFT;

            fourWayMap[SOUTH][DOWN] = ANY_SIDE;
            fourWayMap[SOUTH][UP] = ANY_SIDE;
            fourWayMap[SOUTH][NORTH] = REAR;
            fourWayMap[SOUTH][SOUTH] = FRONT;
            fourWayMap[SOUTH][WEST] = LEFT;
            fourWayMap[SOUTH][EAST] = RIGHT;

            fourWayMap[WEST][DOWN] = ANY_SIDE;
            fourWayMap[WEST][UP] = ANY_SIDE;
            fourWayMap[WEST][NORTH] = LEFT;
            fourWayMap[WEST][SOUTH] = RIGHT;
            fourWayMap[WEST][WEST] = FRONT;
            fourWayMap[WEST][EAST] = REAR;

            fourWayMap[EAST][DOWN] = ANY_SIDE;
            fourWayMap[EAST][UP] = ANY_SIDE;
            fourWayMap[EAST][NORTH] = RIGHT;
            fourWayMap[EAST][SOUTH] = LEFT;
            fourWayMap[EAST][WEST] = REAR;
            fourWayMap[EAST][EAST] = FRONT;
        }

        private String key;

        RelativeSide(String key) {
            this.key = key;
        }

        public String getTranslationKey() {
            return key;
        }

        public static RelativeSide getSideViewed(RotationType t, Direction facing, @Nullable Direction side) {
            if (side != null) {
                if (t == RotationType.FOUR_WAY) {
                    return fourWayMap[side.ordinal()][facing.ordinal()];
                } else if (t == RotationType.SIX_WAY) {
                    return sixWayMap[side.ordinal()][facing.ordinal()];
                }
            }
            return ANY_SIDE;
        }

        @Nullable
        public static Direction getMCSideToAccess(RotationType t, Direction facing, RelativeSide access) {
            RelativeSide[][] map = t == RotationType.FOUR_WAY ? fourWayMap : sixWayMap;
            for (int x = 0; x < map.length; x++) {
                if (map[x][facing.ordinal()] == access) {
                    return Direction.values()[x];
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }
}
