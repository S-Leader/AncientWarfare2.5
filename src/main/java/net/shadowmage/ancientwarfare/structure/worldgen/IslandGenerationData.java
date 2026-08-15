package net.shadowmage.ancientwarfare.structure.worldgen;

import net.minecraft.nbt.CompoundTag;
import net.shadowmage.ancientwarfare.core.compat.WorldSavedData;

/** Persistent construction state for one large natural island per dimension. */
public final class IslandGenerationData extends WorldSavedData {
    private Task active;

    public IslandGenerationData(String name) {
        super(name);
    }

    public Task active() {
        return active;
    }

    public void setActive(Task task) {
        active = task;
        markDirty();
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        if (!tag.contains("active")) {
            active = null;
            return;
        }
        CompoundTag value = tag.getCompound("active");
        Task task = new Task(
                value.getString("template"),
                value.getInt("x"), value.getInt("y"), value.getInt("z"),
                value.getInt("facing"), value.getInt("clusterValue"), value.getBoolean("unique"));
        try {
            task.phase = Phase.valueOf(value.getString("phase"));
        } catch (IllegalArgumentException ignored) {
            task.phase = Phase.UNDERFILL;
        }
        task.progress = Math.max(0, value.getInt("progress"));
        task.structureReserved = !value.contains("structureReserved") || value.getBoolean("structureReserved");
        task.clusterReserved = !value.contains("clusterReserved") || value.getBoolean("clusterReserved");
        if (task.phase != Phase.COMPLETE) {
            active = task;
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        if (active == null || active.phase == Phase.COMPLETE) {
            return tag;
        }
        CompoundTag value = new CompoundTag();
        value.putString("template", active.templateName);
        value.putInt("x", active.x);
        value.putInt("y", active.y);
        value.putInt("z", active.z);
        value.putInt("facing", active.facing);
        value.putInt("clusterValue", active.clusterValue);
        value.putBoolean("unique", active.unique);
        value.putString("phase", active.phase.name());
        value.putInt("progress", active.progress);
        value.putBoolean("structureReserved", active.structureReserved);
        value.putBoolean("clusterReserved", active.clusterReserved);
        tag.put("active", value);
        return tag;
    }

    public enum Phase {
        UNDERFILL,
        BUILD,
        BIOME,
        FINALIZE,
        COMPLETE,
        FAILED
    }

    public static final class Task {
        public final String templateName;
        public final int x;
        public final int y;
        public final int z;
        public final int facing;
        public final int clusterValue;
        public final boolean unique;
        public Phase phase = Phase.UNDERFILL;
        public int progress;
        public boolean structureReserved = true;
        public boolean clusterReserved = true;

        public Task(String templateName, int x, int y, int z, int facing, int clusterValue, boolean unique) {
            this.templateName = templateName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.facing = facing;
            this.clusterValue = clusterValue;
            this.unique = unique;
        }
    }
}
