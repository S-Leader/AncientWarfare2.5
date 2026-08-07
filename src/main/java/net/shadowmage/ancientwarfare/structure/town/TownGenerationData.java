package net.shadowmage.ancientwarfare.structure.town;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.shadowmage.ancientwarfare.core.compat.WorldSavedData;

import java.util.ArrayList;
import java.util.List;

/** Persistent state for phased Ancient Warfare town construction. */
public final class TownGenerationData extends WorldSavedData {
    private final List<Task> tasks = new ArrayList<>();

    public TownGenerationData(String name) {
        super(name);
    }

    public List<Task> tasks() {
        return tasks;
    }

    public Task active() {
        for (Task task : tasks) {
            if (task.phase != Phase.COMPLETE && task.phase != Phase.FAILED) {
                return task;
            }
        }
        return null;
    }

    public boolean contains(int centerX, int centerZ) {
        return tasks.stream().anyMatch(t -> t.centerX() == centerX && t.centerZ() == centerZ
                && t.phase != Phase.FAILED);
    }

    public void add(Task task) {
        tasks.add(task);
        markDirty();
    }

    public void remove(Task task) {
        tasks.remove(task);
        markDirty();
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        tasks.clear();
        ListTag list = tag.getList("tasks", Tag.TAG_COMPOUND);
        for (Tag raw : list) {
            CompoundTag value = (CompoundTag) raw;
            Task task = new Task(
                    value.getString("template"),
                    value.getInt("chunkMinX"), value.getInt("chunkMinZ"),
                    value.getInt("chunkMaxX"), value.getInt("chunkMaxZ"),
                    value.getInt("minY"), value.getInt("maxY"), value.getInt("clusterValue"));
            try {
                task.phase = Phase.valueOf(value.getString("phase"));
            } catch (IllegalArgumentException ignored) {
                task.phase = Phase.PREPARE;
            }
            task.progress = Math.max(0, value.getInt("progress"));
            task.clusterReserved = !value.contains("clusterReserved") || value.getBoolean("clusterReserved");
            if (task.phase != Phase.COMPLETE) {
                tasks.add(task);
            }
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Task task : tasks) {
            if (task.phase == Phase.COMPLETE) {
                continue;
            }
            CompoundTag value = new CompoundTag();
            value.putString("template", task.templateName);
            value.putInt("chunkMinX", task.chunkMinX);
            value.putInt("chunkMinZ", task.chunkMinZ);
            value.putInt("chunkMaxX", task.chunkMaxX);
            value.putInt("chunkMaxZ", task.chunkMaxZ);
            value.putInt("minY", task.minY);
            value.putInt("maxY", task.maxY);
            value.putInt("clusterValue", task.clusterValue);
            value.putString("phase", task.phase.name());
            value.putInt("progress", task.progress);
            value.putBoolean("clusterReserved", task.clusterReserved);
            list.add(value);
        }
        tag.put("tasks", list);
        return tag;
    }

    public enum Phase {
        PREPARE,
        WALLS,
        ROADS,
        BUILDINGS,
        LAMPS,
        FINALIZE,
        COMPLETE,
        FAILED
    }

    public static final class Task {
        public final String templateName;
        public final int chunkMinX;
        public final int chunkMinZ;
        public final int chunkMaxX;
        public final int chunkMaxZ;
        public final int minY;
        public final int maxY;
        public final int clusterValue;
        public Phase phase = Phase.PREPARE;
        public int progress;
        public boolean clusterReserved = true;

        public Task(String templateName, int chunkMinX, int chunkMinZ, int chunkMaxX, int chunkMaxZ,
                    int minY, int maxY, int clusterValue) {
            this.templateName = templateName;
            this.chunkMinX = chunkMinX;
            this.chunkMinZ = chunkMinZ;
            this.chunkMaxX = chunkMaxX;
            this.chunkMaxZ = chunkMaxZ;
            this.minY = minY;
            this.maxY = maxY;
            this.clusterValue = clusterValue;
        }

        public TownBoundingArea area() {
            return new TownBoundingArea(chunkMinX, chunkMinZ, chunkMaxX, chunkMaxZ, minY, maxY);
        }

        public int centerX() {
            return ((chunkMinX * 16) + (chunkMaxX * 16 + 15)) / 2;
        }

        public int centerZ() {
            return ((chunkMinZ * 16) + (chunkMaxZ * 16 + 15)) / 2;
        }
    }
}
