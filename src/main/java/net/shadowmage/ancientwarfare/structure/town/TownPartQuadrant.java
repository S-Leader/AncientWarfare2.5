package net.shadowmage.ancientwarfare.structure.town;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.shadowmage.ancientwarfare.core.util.Trig;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;

import java.util.Collections;
import java.util.List;

public class TownPartQuadrant {

    protected StructureBB bb;
    public final TownGenerator gen;
    private Direction xDir, zDir;
    protected int xDivs, zDivs;
    private boolean roadBorders[];
    protected TownPartBlock blocks[];

    public TownPartQuadrant(Direction xDir, Direction zDir, StructureBB bb, boolean[] borders, TownGenerator gen) {
        this.xDir = xDir;
        this.zDir = zDir;
        this.bb = bb;
        this.roadBorders = borders;
        this.gen = gen;
    }

    public boolean hasRoadBorder(Direction d) {
        return roadBorders[d.get2DDataValue()];
    }

    protected void setRoadBorder(Direction d, boolean val) {
        roadBorders[d.get2DDataValue()] = val;
    }

    public void subdivide(int blockSize, int plotSize, boolean gridRoads) {
        int totalWidth = (bb.max.getX() - bb.min.getX());
        int totalLength = (bb.max.getZ() - bb.min.getZ());
        int widthToUse = totalWidth;
        int lengthToUse = totalLength;

        int y1 = gen.maximalBounds.min.getY();
        int y2 = gen.maximalBounds.max.getY();

        widthToUse--;//the forced road edge for first block
        lengthToUse--;//the forced road edge for first block
        while (widthToUse > 0) {
            widthToUse -= blockSize;
            widthToUse -= 2;//end edge of block + front edge of next block
            xDivs++;
        }
        while (lengthToUse > 0) {
            lengthToUse -= blockSize;
            lengthToUse -= 2;
            zDivs++;
        }

        blocks = new TownPartBlock[xDivs * zDivs];
        int xStart, xEnd;
        int zStart, zEnd;
        int xSize, zSize;
        int xIndex, zIndex;
        TownPartBlock block;
        float distFromTownCenter = 0;
        StructureBB sbb;
        boolean[] borders;

        widthToUse = totalWidth;
        xStart = xDir.getStepX() < 0 ? bb.max.getX() - 1 : bb.min.getX() + 1;
        for (int x = 0; x < xDivs; x++) {
            xSize = widthToUse > blockSize ? blockSize : widthToUse;
            xEnd = xStart + xDir.getStepX() * (xSize - 1);
            xIndex = xDir == Direction.WEST ? (xDivs - 1) - x : x;

            zStart = zDir.getStepZ() < 0 ? bb.max.getZ() - 1 : bb.min.getZ() + 1;
            lengthToUse = (bb.max.getZ() - bb.min.getZ());
            for (int z = 0; z < zDivs; z++) {
                zSize = lengthToUse > blockSize ? blockSize : lengthToUse;
                zEnd = zStart + zDir.getStepZ() * (zSize - 1);
                zIndex = zDir == Direction.NORTH ? (zDivs - 1) - z : z;

                sbb = new StructureBB(new BlockPos(xStart, y1, zStart), new BlockPos(xEnd, y2, zEnd));
                borders = gridRoads ? getBordersGrid(xIndex, zIndex) : getBordersExterior(x, z);
                distFromTownCenter = Trig.getDistance(sbb.getCenterX(), y1, sbb.getCenterZ(), gen.maximalBounds.getCenterX(), y1, gen.maximalBounds.getCenterZ());
                block = new TownPartBlock(this, sbb, xIndex, zIndex, borders, distFromTownCenter);

                setBlock(block, xIndex, zIndex);
                block.subdivide(plotSize);

                lengthToUse -= (blockSize + 2);
                zStart = zEnd + zDir.getStepZ() * 3;
            }

            widthToUse -= (blockSize + 2);
            xStart = xEnd + xDir.getStepX() * 3;
        }
    }

    private void setBlock(TownPartBlock tb, int x, int z) {
        blocks[getIndex(x, z)] = tb;
    }

    protected TownPartBlock getBlock(int x, int z) {
        return blocks[getIndex(x, z)];
    }

    private int getIndex(int x, int z) {
        return z * xDivs + x;
    }

    private boolean[] getBordersGrid(int x, int z) {
        boolean[] borders = new boolean[4];
        if (zDir == Direction.NORTH) {
            borders[Direction.SOUTH.get2DDataValue()] = true;//has south
            borders[Direction.NORTH.get2DDataValue()] = z > 0;//not on northern edge
        } else//zDir==Direction.SOUTH
        {
            borders[Direction.NORTH.get2DDataValue()] = true;//has south
            borders[Direction.SOUTH.get2DDataValue()] = z < zDivs - 1;//not on souther edge
        }
        if (xDir == Direction.WEST) {
            borders[Direction.EAST.get2DDataValue()] = true;//has east
            borders[Direction.WEST.get2DDataValue()] = x > 0;
        } else {
            borders[Direction.WEST.get2DDataValue()] = true;
            borders[Direction.EAST.get2DDataValue()] = x < xDivs - 1;
        }
        return borders;
    }

    private boolean[] getBordersExterior(int x, int z) {
        boolean[] borders = new boolean[4];
        borders[Direction.WEST.get2DDataValue()] = roadBorders[Direction.WEST.get2DDataValue()] && x == 0;
        borders[Direction.EAST.get2DDataValue()] = roadBorders[Direction.EAST.get2DDataValue()] && x == 0;
        borders[Direction.NORTH.get2DDataValue()] = roadBorders[Direction.NORTH.get2DDataValue()] && z == 0;
        borders[Direction.SOUTH.get2DDataValue()] = roadBorders[Direction.SOUTH.get2DDataValue()] && z == 0;
        return borders;
    }

    public void addBlocks(List<TownPartBlock> blocks) {
        Collections.addAll(blocks, this.blocks);
    }

    public Direction getXDir() {
        return xDir;
    }

    public Direction getZDir() {
        return zDir;
    }

}
