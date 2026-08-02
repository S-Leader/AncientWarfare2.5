package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NpcAIDoor extends NpcAI<NpcBase> {
    private final boolean close;
    private final Set<BlockPos> doorPositions = new HashSet<>();
    private static final int RECHECK_INTERVAL = 40;
    private int doorCheckCooldown = RECHECK_INTERVAL;

    public NpcAIDoor(NpcBase npc, boolean closeBehind) {
        super(npc);
        close = closeBehind;
    }

    @Override
    public final boolean canUse() {
        if (!super.canUse()) {
            return false;
        }

        if (!doorPositions.isEmpty()) {
            return true;
        }

        GroundPathNavigation pathnavigate = (GroundPathNavigation) npc.getNavigation();
        if (!pathnavigate.canOpenDoors() || pathnavigate.isDone()) {
            return false;
        }

        Path path = pathnavigate.getPath();
        if (path == null) {
            return false;
        }

        if (addDoorCloseOnThePath(path)) {
            return true;
        }

        if (!npc.horizontalCollision) {
            return false;
        }
        BlockPos potentialDoorPos = new BlockPos(Mth.floor(npc.getX()), Mth.floor(npc.getY()), Mth.floor(npc.getZ()));
        return findDoor(potentialDoorPos) || findDoor(potentialDoorPos.above());
    }

    private boolean addDoorCloseOnThePath(Path path) {
        for (int i = Math.max(path.getNextNodeIndex() - 1, 0); i < Math.min(path.getNextNodeIndex() + 2, path.getNodeCount()); ++i) {
            Node pathpoint = path.getNode(i);

            if (npc.distanceToSqr(pathpoint.x + 0.5D, npc.getY(), pathpoint.z + 0.5D) <= 1.5D) {
                BlockPos potentialDoorPos = new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z);
                if (findDoor(potentialDoorPos)) {
                    interactWithDoor(potentialDoorPos, true);
                    return true;
                } else if (findDoor(potentialDoorPos.above())) {
                    interactWithDoor(potentialDoorPos.above(), true);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        closeTooFarAwayDoor();
        recheckDoorOpen();
        addDoorCloseOnThePath();
    }

    private void closeTooFarAwayDoor() {
        Iterator<BlockPos> it = doorPositions.iterator();
        while (it.hasNext()) {
            BlockPos doorPos = it.next();
            if (!(isCloseToDoor(doorPos) || isFriendlyInDoor(doorPos))) {
                it.remove();
                if (close) {
                    interactWithDoor(doorPos, false);
                }
            }
        }
    }

    private void addDoorCloseOnThePath() {
        Path path = npc.getNavigation().getPath();
        if (path == null) {
            return;
        }
        addDoorCloseOnThePath(path);
    }

    private void recheckDoorOpen() {
        if (doorCheckCooldown <= 0) {
            doorCheckCooldown = RECHECK_INTERVAL;
            doorPositions.forEach(doorPos -> interactWithDoor(doorPos, true));
        } else {
            doorCheckCooldown--;
        }
    }

    private boolean isFriendlyInDoor(BlockPos doorPos) {
        Vec3 doorCenter = Vec3.atCenterOf(doorPos);
        return !npc.level().getEntitiesOfClass(NpcBase.class,
                new AABB(doorCenter.x, doorCenter.y, doorCenter.z, doorCenter.x, doorCenter.y, doorCenter.z).inflate(2.1D),
                n -> n != null && !n.isHostileTowards(npc)).isEmpty();
    }

    private boolean isCloseToDoor(BlockPos doorPos) {
        return doorPos.distToCenterSqr(npc.getX(), npc.getY(), npc.getZ()) <= 2D;
    }

    private boolean isDoor(BlockPos potentialDoorPos) {
        BlockState doorState = npc.level().getBlockState(potentialDoorPos);
        if (doorState.getBlock() instanceof DoorBlock) {
            return doorState.is(BlockTags.WOODEN_DOORS);
        } else {
            return doorState.getBlock() instanceof FenceGateBlock;
        }
    }

    private boolean findDoor(BlockPos potentialDoorPos) {
        if (isDoor(potentialDoorPos) && !doorPositions.contains(potentialDoorPos)) {
            doorPositions.add(potentialDoorPos);
            return true;
        }
        return false;
    }

    private void interactWithDoor(BlockPos doorPos, boolean isOpening) {
        BlockState doorState = npc.level().getBlockState(doorPos);
        if (doorState.getBlock() instanceof DoorBlock) {
            ((DoorBlock) doorState.getBlock()).setOpen(npc, npc.level(), doorState, doorPos, isOpening);
        } else if (doorState.getBlock() instanceof FenceGateBlock) {
            interactWithFenceGate(doorPos, isOpening, doorState);
        }
    }

    private void interactWithFenceGate(BlockPos doorPos, boolean isOpening, BlockState doorState) {
        boolean fenceGateOpen = doorState.getValue(FenceGateBlock.OPEN);
        if (isOpening) {
            if (!fenceGateOpen) {
                Direction entityFacing = Direction.fromYRot(npc.getYRot());
                openFenceGate(doorState, doorPos, entityFacing);
                BlockState state = npc.level().getBlockState(doorPos.above());
                if (state.getBlock() instanceof FenceGateBlock) {
                    openFenceGate(state, doorPos.above(), entityFacing);
                }
            }
        } else {
            doorState = doorState.setValue(FenceGateBlock.OPEN, false);
            npc.level().setBlock(doorPos, doorState, 10);
            BlockState state = npc.level().getBlockState(doorPos.above());
            if (state.getBlock() instanceof FenceGateBlock) {
                state = state.setValue(FenceGateBlock.OPEN, false);
                npc.level().setBlock(doorPos.above(), state, 10);
            }
        }
        npc.level().levelEvent(null, Boolean.TRUE.equals(doorState.getValue(FenceGateBlock.OPEN)) ? 1008 : 1014, doorPos, 0);
    }

    private void openFenceGate(BlockState state, BlockPos pos, Direction entityFacing) {
        BlockState updatedState = state;
        if (updatedState.getValue(HorizontalDirectionalBlock.FACING) == entityFacing.getOpposite()) {
            updatedState = updatedState.setValue(HorizontalDirectionalBlock.FACING, entityFacing);
        }

        updatedState = updatedState.setValue(FenceGateBlock.OPEN, true);
        npc.level().setBlock(pos, updatedState, 10);
    }
}
