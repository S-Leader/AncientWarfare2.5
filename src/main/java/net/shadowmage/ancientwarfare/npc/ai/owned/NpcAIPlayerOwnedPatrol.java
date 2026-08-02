package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.orders.CombatOrder;

public class NpcAIPlayerOwnedPatrol extends NpcAI<NpcBase> {
    private static final int MAX_TICKS_AT_POINT = 50;//default 2.5 second idle at each point

    private boolean init = false;
    private int patrolIndex;
    private boolean atPoint;
    private int ticksAtPoint;
    private CombatOrder orders;
    private ItemStack ordersStack;

    public NpcAIPlayerOwnedPatrol(NpcBase npc) {
        super(npc);
        setMutexBits(ATTACK + MOVE);
    }

    public void onOrdersInventoryChanged() {
        patrolIndex = 0;
        ordersStack = npc.ordersStack;
        orders = CombatOrder.getCombatOrder(ordersStack);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        if (!init) {
            init = true;
            ordersStack = npc.ordersStack;
            orders = CombatOrder.getCombatOrder(ordersStack);
            if (orders == null || patrolIndex >= orders.size()) {
                patrolIndex = 0;
            }
        }
        if (npc.getTarget() != null) {
            return false;
        }
        return orders != null && !ordersStack.isEmpty() && orders.getPatrolDimension() == getLegacyDimensionId(npc.level()) && !orders.isEmpty();
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
    public void start() {
        npc.addAITask(TASK_PATROL);
    }

    @Override
    public void tick() {
        if (atPoint) {
            npc.removeAITask(TASK_MOVE);
            ticksAtPoint++;
            if (ticksAtPoint > MAX_TICKS_AT_POINT) {
                setMoveToNextPoint();
            }
        } else {
            BlockPos pos = orders.get(patrolIndex);
            double dist = npc.distanceToSqr(pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d);
            if (dist > 2.d * 2.d) {
                moveToPosition(pos, dist);
            } else {
                atPoint = true;
                ticksAtPoint = 0;
            }
        }
    }

    private void setMoveToNextPoint() {
        atPoint = false;
        ticksAtPoint = 0;
        patrolIndex++;
        moveRetryDelay = 0;
        if (patrolIndex >= orders.size()) {
            patrolIndex = 0;
        }
    }

    @Override
    public void stop() {
        ticksAtPoint = 0;
        moveRetryDelay = 0;
        npc.removeAITask(TASK_PATROL + TASK_MOVE);
    }

    public void readFromNBT(CompoundTag tag) {
        patrolIndex = tag.getInt("patrolIndex");
        atPoint = tag.getBoolean("atPoint");
        ticksAtPoint = tag.getInt("ticksAtPoint");
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.putInt("patrolIndex", patrolIndex);
        tag.putBoolean("atPoint", atPoint);
        tag.putInt("ticksAtPoint", ticksAtPoint);
        return tag;
    }

}
