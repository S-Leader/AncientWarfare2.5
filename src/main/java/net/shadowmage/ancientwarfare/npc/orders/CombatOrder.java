package net.shadowmage.ancientwarfare.npc.orders;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.OrderingList;
import net.shadowmage.ancientwarfare.npc.item.ItemCombatOrder;

public class CombatOrder extends OrderingList<BlockPos> implements INBTSerializable<CompoundTag> {

    int patrolDimensionId = 0;

    public CombatOrder() {

    }

    public void addPatrolPoint(Level world, BlockPos point) {
        if (getLegacyDimensionId(world) != patrolDimensionId) {
            clear();
            patrolDimensionId = getLegacyDimensionId(world);
        }
        add(point);
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

    public int getPatrolDimension() {
        return patrolDimensionId;
    }

    public static CombatOrder getCombatOrder(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemCombatOrder) {
            CombatOrder order = new CombatOrder();
            if (stack.hasTag() && stack.getTag().contains("orders")) {
                order.deserializeNBT(stack.getTag().getCompound("orders"));
            }
            return order;
        }
        return null;
    }

    public void write(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof ItemCombatOrder) {
            stack.getOrCreateTag().put("orders", serializeNBT());
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("dim", patrolDimensionId);
        if (!isEmpty()) {
            ListTag list = new ListTag();
            for (BlockPos point : points) {
                list.add(LongTag.valueOf(point.asLong()));
            }
            tag.put("pointList", list);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        clear();
        patrolDimensionId = tag.getInt("dim");
        if (tag.contains("pointList")) {
            ListTag list = tag.getList("pointList", Constants.NBT.TAG_LONG);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) instanceof LongTag positionTag) {
                    add(BlockPos.of(positionTag.getAsLong()));
                }
            }
        }
    }
}
