package net.shadowmage.ancientwarfare.structure.template.build.validation.border.points;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SmoothingPoint {
    private PointType type;
    private int x;
    private int z;
    private BlockPos worldPos;
    private BlockPos smoothedPos;
    private boolean smoothedPosSet = false;
    private BlockState blockState;
    private boolean useStateForBlending = false;
    private SmoothingPoint outerBorderPoint = null;
    private SmoothingPoint referencePoint;
    private SmoothingPoint closestBorderPoint;
    private int structureBorderDistance;

    private int waterLevel = 0;

    public SmoothingPoint(int x, int z, BlockPos worldPos, PointType type) {
        this.x = x;
        this.z = z;
        this.worldPos = worldPos;
        this.smoothedPos = worldPos;
        this.type = type;
    }

    public void setBlockState(BlockState blockState) {
        this.blockState = blockState;
        useStateForBlending = true;
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public boolean useStateForBlending() {
        return useStateForBlending;
    }

    public BlockPos getSmoothedPos() {
        return smoothedPos;
    }

    public BlockPos getWorldPos() {
        return worldPos;
    }

    public boolean hasSmoothedPosSet() {
        return smoothedPosSet;
    }

    public void setSmoothedPos(BlockPos smoothedPos) {
        this.smoothedPos = smoothedPos;
        smoothedPosSet = true;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getStructureBorderDistance() {
        return structureBorderDistance;
    }

    public void setStructureBorder(SmoothingPoint closestBorderPoint, int structureBorderDistance) {
        this.closestBorderPoint = closestBorderPoint;
        this.structureBorderDistance = structureBorderDistance;
    }

    public void setOuterBorderAndReferencePoint(SmoothingPoint outerBorder, SmoothingPoint referencePoint) {
        this.outerBorderPoint = outerBorder;
        this.referencePoint = referencePoint;
    }

    public SmoothingPoint getReferencePoint() {
        return referencePoint;
    }

    public SmoothingPoint getOuterBorderPoint() {
        return outerBorderPoint;
    }

    public SmoothingPoint getClosestBorderPoint() {
        return closestBorderPoint;
    }

    public PointType getType() {
        return type;
    }

    public void setWaterLevel(int waterLevel) {
        this.waterLevel = waterLevel;
    }

    public int getWaterLevel() {
        return waterLevel;
    }
}
