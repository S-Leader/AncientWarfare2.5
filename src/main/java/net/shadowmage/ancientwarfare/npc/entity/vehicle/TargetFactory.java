package net.shadowmage.ancientwarfare.npc.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.Optional;

public class TargetFactory {
    private TargetFactory() {
    }

    public static Optional<CompoundTag> serializeNBT(ITarget target, CompoundTag tag) {
        if (target instanceof BlockPosTarget) {
            ((BlockPosTarget) target).serializeToNBT(tag);
            return Optional.of(tag);
        }

        //entity target doesn't need serializing as it will be recreated by setAttackTarget call on entity load
        return Optional.empty();
    }

    public static ITarget deserializeFromNBT(CompoundTag tag) {
        if (tag.contains("targetPos")) {
            return new BlockPosTarget(BlockPos.of(tag.getLong("targetPos")));
        }
        return NONE;
    }

    public static final ITarget NONE = new ITarget() {
        @Override
        public double getX() {
            return 0;
        }

        @Override
        public double getY() {
            return 0;
        }

        @Override
        public double getZ() {
            return 0;
        }

        private final AABB noBounds = new AABB(0, 0, 0, 0, 0, 0);

        @Override
        public AABB getBoundigBox() {
            return noBounds;
        }

        @Override
        public boolean exists(Level entityWorld) {
            return false;
        }
    };
}
