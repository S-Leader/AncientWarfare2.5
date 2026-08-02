package net.shadowmage.ancientwarfare.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public final class RayTraceUtils {
    private RayTraceUtils() {
    }

    @Nullable
    public static HitResult getPlayerTarget(Player player, float range, float border) {
        HashSet<Entity> excluded = new HashSet<>();
        excluded.add(player);
        if (player.getVehicle() != null) {
            excluded.add(player.getVehicle());
        }
        Vec3 start = player.getEyePosition(1.0F);
        Vec3 end = start.add(player.getViewVector(1.0F).scale(range));
        return tracePath(player.level(), start.x, start.y, start.z, end.x, end.y, end.z, border, excluded);
    }

    @Nullable
    public static HitResult tracePathWithYawPitch(Level world, float x, float y, float z, float yaw, float pitch,
                                                  float range, float borderSize, HashSet<Entity> excluded) {
        float tx = x + Trig.sinDegrees(yaw + 180) * range * Trig.cosDegrees(pitch);
        float ty = -Trig.sinDegrees(pitch) * range + y;
        float tz = z + Trig.cosDegrees(yaw) * range * Trig.cosDegrees(pitch);
        return tracePath(world, x, y, z, tx, ty, tz, borderSize, excluded);
    }

    /**
     * Returns the nearest pickable entity or block intersected by the supplied segment.
     */
    @Nullable
    public static HitResult tracePath(Level world, double x, double y, double z, double tx, double ty, double tz,
                                      float borderSize, HashSet<Entity> excluded) {
        Vec3 start = new Vec3(x, y, z);
        Vec3 end = new Vec3(tx, ty, tz);
        Entity source = excluded.stream().findFirst().orElse(null);
        BlockHitResult blockHit = world.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE, source));
        double closestDistanceSq = blockHit.getType() == HitResult.Type.MISS
                ? start.distanceToSqr(end)
                : start.distanceToSqr(blockHit.getLocation());

        AABB searchBox = new AABB(start, end).inflate(borderSize);
        Entity closestEntity = null;
        Vec3 closestLocation = null;
        for (Entity entity : world.getEntities(source, searchBox,
                candidate -> candidate.isPickable() && !excluded.contains(candidate))) {
            AABB targetBox = entity.getBoundingBox().inflate(entity.getPickRadius() + borderSize);
            Optional<Vec3> intercept = targetBox.clip(start, end);
            if (intercept.isPresent()) {
                double distanceSq = start.distanceToSqr(intercept.get());
                if (distanceSq < closestDistanceSq) {
                    closestDistanceSq = distanceSq;
                    closestEntity = entity;
                    closestLocation = intercept.get();
                }
            }
        }

        if (closestEntity != null) {
            return new EntityHitResult(closestEntity, closestLocation);
        }
        return blockHit.getType() == HitResult.Type.MISS ? null : blockHit;
    }

    @Nullable
    public static HitResult raytraceMultiAABB(List<AABB> aabbs, BlockPos pos, Vec3 start, Vec3 end) {
        return raytraceMultiAABB(aabbs, pos, start, end, (hit, box) -> hit);
    }

    @Nullable
    public static <T> T raytraceMultiAABB(List<AABB> aabbs, BlockPos pos, Vec3 start, Vec3 end,
                                          Function2<HitResult, AABB, T> getValue) {
        T result = null;
        double closestDistanceSq = Double.MAX_VALUE;
        for (AABB localBox : aabbs) {
            AABB worldBox = localBox.move(pos);
            Optional<Vec3> intercept = worldBox.clip(start, end);
            if (intercept.isEmpty()) {
                continue;
            }
            double distanceSq = intercept.get().distanceToSqr(start);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                Direction face = getHitFace(worldBox, intercept.get());
                result = getValue.apply(new BlockHitResult(intercept.get(), face, pos, false), localBox);
            }
        }
        return result;
    }

    private static Direction getHitFace(AABB box, Vec3 hit) {
        final double epsilon = 1.0E-5D;
        if (Math.abs(hit.x - box.minX) < epsilon) return Direction.WEST;
        if (Math.abs(hit.x - box.maxX) < epsilon) return Direction.EAST;
        if (Math.abs(hit.y - box.minY) < epsilon) return Direction.DOWN;
        if (Math.abs(hit.y - box.maxY) < epsilon) return Direction.UP;
        if (Math.abs(hit.z - box.minZ) < epsilon) return Direction.NORTH;
        return Direction.SOUTH;
    }

    public static AABB getSelectedBoundingBox(List<AABB> aabbs, BlockPos pos, Player player) {
        Vec3 start = player.getEyePosition(1.0F);
        double reach = player.isCreative() ? 6.0D : 4.5D;
        Vec3 end = start.add(player.getViewVector(1.0F).scale(reach));
        AABB selected = raytraceMultiAABB(aabbs, pos, start, end, (hit, aabb) -> aabb);
        return (selected == null ? aabbs.get(0) : selected).move(pos);
    }
}
