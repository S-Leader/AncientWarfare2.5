package net.shadowmage.ancientwarfare.vehicle.pathing;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.core.util.Trig;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.entity.EntityGate;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.tile.TEGateProxy;
import net.shadowmage.ancientwarfare.vehicle.entity.IPathableEntity;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

import java.util.List;
import java.util.Random;

public class Navigator implements IPathableCallback {
    private static final int DOOR_OPEN_MAX = 15;
    private static final int DOOR_CHECK_TICKS_MAX = 5;

    private PathFinderThetaStar pathFinder = new PathFinderThetaStar();

    protected IPathableEntity owner;
    protected Entity entity;
    protected PathWorldAccess world;
    protected EntityPath path;
    private final Node finalTarget = new Node(0, 0, 0);
    private Node currentTarget;
    protected Random rng = new Random();

    protected EntityGate gate = null;
    private boolean hasDoor = false;
    private BlockPos doorPos = BlockPos.ZERO;
    private int doorOpenTicks = 0;
    private int doorCheckTicks = 0;
    private Vec3 stuckCheckPosition = Vec3.ZERO;
    private int stuckCheckTicks = 40;
    private int stuckCheckTicksMax = 40;
    private double airborneStartY;
    private boolean wasAirborne;

    private PathFinderCrawler testCrawler;

    public Navigator(VehicleBase owner) {
        this.owner = owner;
        this.entity = owner.getEntity();
        this.world = owner.worldAccess;
        this.path = new EntityPath();
        finalTarget.reassign(Mth.floor(entity.getX()), Mth.floor(entity.getY()), Mth.floor(entity.getZ()));
        this.stuckCheckPosition = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        this.testCrawler = new PathFinderCrawler();
    }

    public void setStuckCheckTicks(int ticks) {
        if (ticks > 0) {
            this.stuckCheckTicksMax = ticks;
        }
    }

    public void setMoveToTarget(BlockPos pos) {
        if (!entity.level().hasChunkAt(pos)) {
            return;
        }
        this.sendToClients(pos);
        int ex = Mth.floor(entity.getX());
        int ey = Mth.floor(entity.getY());
        int ez = Mth.floor(entity.getZ());
        if (entity.getY() % 1.f > 0.75 && !world.isWalkable(ex, ey, ez)) {
            ey++;
        }
        if (this.shouldCalculatePath(pos)) {
            this.finalTarget.reassign(pos);
            this.calculatePath(ex, ey, ez, pos);
        }
    }

    public void onMovementUpdate() {
        this.detectFall();
        this.updateMoveHelper();
        this.detectStuck();
        this.claimNode();
        if (this.currentTarget != null) {
            if (this.world.canUseLaders) {
                this.handleLadderMovement();
            }
            if (this.world.canOpenDoors) {
                this.doorInteraction();
            }
            owner.setMoveTo(currentTarget.x + 0.5d, currentTarget.y, currentTarget.z + 0.5d, owner.getDefaultMoveSpeed());
        }
    }

    private void detectFall() {
        if (!entity.onGround()) {
            if (!wasAirborne) {
                wasAirborne = true;
                airborneStartY = entity.getY();
            }
            return;
        }
        if (wasAirborne) {
            wasAirborne = false;
            if (airborneStartY - entity.getY() > 1.5D && !isAtTarget(finalTarget.getPos())) {
                calculatePath(Mth.floor(entity.getX()), Mth.floor(entity.getY()), Mth.floor(entity.getZ()), finalTarget.getPos());
            }
        }
    }

    private void handleLadderMovement() {
        if (owner.isPathableEntityOnLadder()) {
            Vec3 movement = entity.getDeltaMovement();
            if (currentTarget.y < (int) entity.getY()) {
                entity.setDeltaMovement(movement.x, -0.125D, movement.z);
            } else if (currentTarget.y > (int) entity.getY()) {
                entity.setDeltaMovement(movement.x, 0.125D, movement.z);
            }
        }
    }

    private void updateMoveHelper() {
        this.pathFinder.doSearchIterations(10);
        if (this.doorOpenTicks > 0) {
            this.doorOpenTicks--;
        }
        if (this.hasDoor && this.doorOpenTicks <= 0) {
            this.hasDoor = false;
            this.interactWithDoor(doorPos, false);
        }
        if (this.gate != null && this.doorOpenTicks <= 0) {
            this.interactWithGate(false);
            this.gate = null;
        }
    }

    private void detectStuck() {
        if (this.stuckCheckTicks <= 0) {
            this.stuckCheckTicks = this.stuckCheckTicksMax;
            if (this.currentTarget != null && Math.sqrt(entity.distanceToSqr(stuckCheckPosition.x, stuckCheckPosition.y, stuckCheckPosition.z)) < 1.5d) {
                this.owner.onStuckDetected();
                this.clearPath();
                this.currentTarget = null;
            }
            stuckCheckPosition = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        } else {
            this.stuckCheckTicks--;
        }
    }

    private boolean isNewTargetClose(BlockPos target) {
        float dist = (float) Math.sqrt(entity.distanceToSqr(finalTarget.x, finalTarget.y, finalTarget.z));
        float tDist = Trig.getDistance(finalTarget.x, finalTarget.y, finalTarget.z, target.getX(), target.getY(), target.getZ());
        return tDist < dist * 0.1f;
    }

    private boolean isNewTarget(BlockPos target) {//
        return !isNewTargetClose(target) && !this.finalTarget.equals(target.getX(), target.getY(), target.getZ());
    }

    private boolean isAtTarget(BlockPos pos) {
        return Math.sqrt(entity.distanceToSqr(pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d)) < entity.getBbWidth();
    }

    private boolean isPathEmpty() {
        return this.path.getActivePathSize() <= 0;
    }

    private boolean shouldCalculatePath(BlockPos target) {
        return isNewTarget(target) || (isPathEmpty() && !isAtTarget(target) && currentTarget == null && !pathFinder.isSearching);
    }

    private void calculatePath(int ex, int ey, int ez, BlockPos target) {
        this.path.clearPath();
        this.currentTarget = null;
        if (PathUtils.canPathStraightToTarget(world, ex, ey, ez, target)) {
            this.currentTarget = new Node(target);
        } else {
            this.path.setPath(testCrawler.findPath(world, ex, ey, ez, target, 8));
            Node end = this.path.getEndNode();
            if (end != null && (end.x != target.getX() || end.y != target.getY() || end.z != target.getZ())) {
                this.pathFinder.findPath(world, end.x, end.y, end.z, target, 60, this, false);
            }
        }
        this.stuckCheckTicks = this.stuckCheckTicksMax;
        stuckCheckPosition = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        Node start = this.path.getFirstNode();
        if (start != null && (getEntityDistance(start) < 0.8f && start.y == ey)) {
            this.path.claimNode();//skip the first node because it is probably behind you, move onto next
        }
        this.claimNode();
    }

    private void doorInteraction() {
        if (this.doorCheckTicks <= 0) {
            this.doorCheckTicks = DOOR_CHECK_TICKS_MAX;
            if (this.entity.horizontalCollision && checkForDoors(entity.blockPosition())) {
                if (this.hasDoor) {
                    this.interactWithDoor(doorPos, true);
                    this.doorOpenTicks = DOOR_OPEN_MAX;
                } else if (gate != null) {
                    this.interactWithGate(true);
                    this.doorOpenTicks = DOOR_OPEN_MAX;
                }
            }
        } else {
            this.doorCheckTicks--;
        }
    }

    private boolean checkForDoors(BlockPos entityPos) {
        BlockState state = entity.level().getBlockState(entityPos);
        Block block = state.getBlock();
        if ((block instanceof DoorBlock && state.is(BlockTags.WOODEN_DOORS)) || block instanceof FenceGateBlock) {
            if (hasDoor && !doorPos.equals(entityPos)) {
                this.interactWithDoor(doorPos, false);
            }
            doorPos = entityPos;
            hasDoor = true;
            return true;
        }
        if (block == AWStructureBlocks.GATE_PROXY) {
            WorldTools.getTile(entity.level(), entityPos, TEGateProxy.class).ifPresent(proxy -> {
                interactWithGate(false);
                gate = proxy.getOwner().orElse(null);
            });
            return true;
        }
        float yaw = entity.getYRot();
        while (yaw < 0) {
            yaw += 360.f;
        }
        while (yaw >= 360.f) {
            yaw -= 360.f;
        }
        int x = entityPos.getX();
        int y = entityPos.getY();
        int z = entityPos.getZ();
        if (yaw >= 360 - 45 || yaw < 45)//south, check z+
        {
            z++;
        } else if (yaw >= 45 && yaw < 45 + 90)//west, check x+
        {
            x--;
        } else if (yaw >= 180 - 45 && yaw < 180 + 45)//north
        {
            z--;
        } else//east
        {
            x++;
        }
        state = entity.level().getBlockState(new BlockPos(x, y, z));
        block = state.getBlock();
        if ((block instanceof DoorBlock && state.is(BlockTags.WOODEN_DOORS)) || block instanceof FenceGateBlock) {
            if (hasDoor && !doorPos.equals(entityPos)) {
                this.interactWithDoor(doorPos, false);
            }
            doorPos = new BlockPos(x, y, z);
            hasDoor = true;
            return true;
        }
        if (block == AWStructureBlocks.GATE_PROXY) {
            WorldTools.getTile(entity.level(), new BlockPos(x, y, z), TEGateProxy.class).ifPresent(proxy -> {
                interactWithGate(false);
                gate = proxy.getOwner().orElse(null);
            });
            return true;
        }
        return false;
    }

    private void interactWithGate(boolean open) {
        if ((gate.edgePosition > 0 && !open) || (gate.edgePosition == 0 && open)) {
            gate.activateGate();
        }
        if (!open) {
            this.gate = null;
        }
    }

    private void interactWithDoor(BlockPos doorPos, boolean open) {
        BlockState state = entity.level().getBlockState(doorPos);
        Block block = state.getBlock();
        if (block instanceof DoorBlock && state.is(BlockTags.WOODEN_DOORS)) {
            ((DoorBlock) block).setOpen(entity, entity.level(), state, doorPos, open);
        } else if (block instanceof FenceGateBlock && open != state.getValue(FenceGateBlock.OPEN)) {
            if (open && !state.getValue(FenceGateBlock.OPEN)) {
                entity.level().setBlock(doorPos, state.setValue(FenceGateBlock.OPEN, true), 2);
                entity.level().levelEvent(null, 1008, doorPos, 0);
            } else if (!open && state.getValue(FenceGateBlock.OPEN)) {
                entity.level().setBlock(doorPos, state.setValue(FenceGateBlock.OPEN, false), 2);
                entity.level().levelEvent(null, 1014, doorPos, 0);
            }
        }
    }

    private void claimNode() {
        if (this.currentTarget == null || this.getEntityDistance(currentTarget) < entity.getBbWidth()) {
            this.currentTarget = this.path.claimNode();
            while (this.currentTarget != null && this.getEntityDistance(currentTarget) < entity.getBbWidth()) {
                this.currentTarget = this.path.claimNode();
            }
            this.stuckCheckTicks = this.stuckCheckTicksMax;
            stuckCheckPosition = new Vec3(entity.getX(), entity.getY(), entity.getZ());
        }

    }

    private float getEntityDistance(Node n) {
        return entity == null ? 0.f : n == null ? 0.f : (float) Math.sqrt(entity.distanceToSqr(n.x + 0.5d, n.y, n.z + 0.5d));
    }

    private void sendToClients(BlockPos pos) {
        //  if(Config.DEBUG && !world.isClientSide() && owner.getEntity() instanceof NpcBase)//relay to client, force client-side to find path as well (debug rendering of path)
        //    {
        //    NBTTagCompound tag = new NBTTagCompound();
        //    tag.putInt("tx", x);
        //    tag.putInt("ty", y);
        //    tag.putInt("tz", z);
        //    Packet04Npc pkt = new Packet04Npc();
        //    pkt.setParams(entity);
        //    pkt.setPathTarget(tag);
        //    pkt.sendPacketToAllTrackingClients(entity);
        //    }
    }

    @Override
    public void onPathFound(List<Node> pathNodes) {
        if (pathNodes == null || pathNodes.isEmpty()) {
            this.owner.onStuckDetected();
            this.clearPath();
            return;
        }
        this.path.addPath(world, pathNodes);
    }

    public void clearPath() {
        this.path.clearPath();
        this.currentTarget = null;
    }

    public void forcePath(List<Node> n) {
        this.path.setPath(n);
        this.claimNode();
    }

    public void setCanGoOnLand(boolean land) {
        this.world.setCanGoOnLand(land);
    }
}
