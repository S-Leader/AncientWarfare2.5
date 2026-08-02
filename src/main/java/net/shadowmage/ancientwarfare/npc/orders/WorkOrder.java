package net.shadowmage.ancientwarfare.npc.orders;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.util.INBTSerializable;
import net.shadowmage.ancientwarfare.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfare.core.interfaces.IWorker;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.OrderingList;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.item.ItemWorkOrder;

import java.util.List;

public class WorkOrder extends OrderingList<WorkOrder.WorkEntry> implements INBTSerializable<CompoundTag> {
    private static final int MAX_SIZE = 8;
    private WorkPriorityType priorityType = WorkPriorityType.ROUTE;
    private boolean nightShift;

    public boolean isNightShift() {
        return nightShift;
    }

    public void toggleShift() {
        nightShift = !nightShift;
    }

    public WorkPriorityType getPriorityType() {
        return priorityType;
    }

    public List<WorkEntry> getEntries() {
        return points;
    }

    //return true if successfully added
    public boolean addWorkPosition(Level world, BlockPos position) {
        if (position != null && size() < MAX_SIZE) {
            add(new WorkEntry(position, getLegacyDimensionId(world), 0));
            return true;
        }
        return false;
    }

    private static int getLegacyDimensionId(Level world) {
        if (Level.OVERWORLD.equals(world.dimension())) {
            return 0;
        }
        if (Level.NETHER.equals(world.dimension())) {
            return -1;
        }
        if (Level.END.equals(world.dimension())) {
            return 1;
        }
        return world.dimension().location().toString().hashCode();
    }

    @Override
    public String toString() {
        return "Work Orders size: " + size() + " of type: " + priorityType;
    }

    public static WorkOrder getWorkOrder(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemWorkOrder) {
            WorkOrder order = new WorkOrder();
            //noinspection ConstantConditions
            if (stack.hasTag() && stack.getTag().contains("orders")) {
                order.deserializeNBT(stack.getTag().getCompound("orders"));
            }
            return order;
        }
        return null;
    }

    public void write(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemWorkOrder) {
            stack.getOrCreateTag().put("orders", serializeNBT());
        }
    }

    public void togglePriority() {
        WorkPriorityType[] type = WorkPriorityType.values();
        priorityType = type[(priorityType.ordinal() + 1) % type.length];
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag entryList = new ListTag();
        for (WorkEntry entry : points) {
            entryList.add(entry.writeToNBT(new CompoundTag()));
        }
        tag.put("entryList", entryList);
        tag.putInt("priorityType", priorityType.ordinal());
        tag.putBoolean("nightShift", nightShift);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        clear();
        ListTag entryList = tag.getList("entryList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < entryList.size(); i++) {
            add(new WorkEntry(entryList.getCompound(i)));
        }
        priorityType = WorkPriorityType.values()[tag.getInt("priorityType")];
        nightShift = tag.getBoolean("nightShift");
    }

    public static final class WorkEntry {

        private BlockPos position;
        private int dimension;
        private int workLength;

        private WorkEntry(CompoundTag tag) {
            readFromNBT(tag);
        }//nbt constructor

        private WorkEntry(BlockPos position, int dimension, int workLength) {
            this.setPosition(position);
            this.dimension = dimension;
            this.setWorkLength(workLength);
        }

        public void readFromNBT(CompoundTag tag) {
            setPosition(BlockPos.of(tag.getLong("pos")));
            dimension = tag.getInt("dim");
            setWorkLength(tag.getInt("length"));
        }

        public CompoundTag writeToNBT(CompoundTag tag) {
            tag.putLong("pos", getPosition().asLong());
            tag.putInt("dim", dimension);
            tag.putInt("length", getWorkLength());
            return tag;
        }

        /*
         * @return the block
         */
        public Block getBlock(Level world) {
            return world.getBlockState(getPosition()).getBlock();
        }

        /*
         * @return the position
         */
        public BlockPos getPosition() {
            return position;
        }

        /*
         * @param position the position to set
         */
        public void setPosition(BlockPos position) {
            this.position = position;
        }

        /*
         * @return the workLength
         */
        public int getWorkLength() {
            return workLength;
        }

        /*
         * @param workLength the workLength to set
         */
        public void setWorkLength(int workLength) {
            this.workLength = workLength;
        }
    }

    public enum WorkPriorityType {
        SITE_NEED {
            @Override
            public int getNextWorkIndex(int current, List<WorkEntry> orders, NpcBase npc) {
                for (int i = 0; i < orders.size(); i++) {
                    if (WorldTools.getTile(npc.level(), orders.get(i).getPosition(), IWorkSite.class)
                            .filter(site -> ((IWorker) npc).canWorkAt(site.getWorkType()) && site.hasWork()).isPresent()) {
                        return i;
                    }
                }
                return 0;
            }
        }, ROUTE, TIMED;

        public int getNextWorkIndex(int current, List<WorkEntry> orders, NpcBase npcBase) {
            if (current + 1 >= orders.size()) {
                return 0;
            }
            return current + 1;
        }

        public boolean isTimed() {
            return this == TIMED;
        }
    }

}
