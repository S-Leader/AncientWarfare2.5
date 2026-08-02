package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.shadowmage.ancientwarfare.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfare.core.interfaces.IWorker;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.orders.WorkOrder;
import net.shadowmage.ancientwarfare.npc.orders.WorkOrder.WorkEntry;

import java.util.Optional;

public class NpcAIPlayerOwnedWork extends NpcAI<NpcBase> {
    private int ticksAtSite;
    private int workIndex;
    private WorkOrder order;
    private boolean init = false;

    public NpcAIPlayerOwnedWork(NpcBase npc) {
        super(npc);
        if (!(npc instanceof IWorker)) {
            throw new IllegalArgumentException("cannot instantiate work ai task on non-worker npc");
        }
        setMutexBits(MOVE + ATTACK);
        ticksAtSite = 0;
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        if (!init) {
            order = WorkOrder.getWorkOrder(npc.ordersStack);
            init = true;
            if (order == null || workIndex >= order.size()) {
                workIndex = 0;
            }
        }
        return !(npc.getFoodRemaining() <= 0 || npc.shouldBeAtHome()) && order != null && !order.isEmpty();
    }

    @Override
    public void tick() {
        WorkEntry entry = order.get(workIndex);
        BlockPos pos = entry.getPosition();
        double dist = npc.getDistanceSq(pos);
        if (dist > ((IWorker) npc).getWorkRangeSq()) {
            npc.setSwingingArms(false);
            npc.addAITask(TASK_MOVE);
            ticksAtSite = 0;
            moveToPosition(pos, dist);
        } else {
            npc.setSwingingArms(true);
            if (dist < 10 || shouldMoveFromTimeAtSite(entry) || shouldMoveFromNoWork()) {
                npc.getNavigation().stop();
                npc.removeAITask(TASK_MOVE);
            }
            workAtSite(entry);
        }
    }

    @Override
    public void start() {
        npc.addAITask(TASK_WORK);
        npc.setSwingingArms(false);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void workAtSite(WorkEntry entry) {
        ticksAtSite++;
        if (ticksAtSite == 1) {
            BlockPos pos = entry.getPosition();
            if (WorldTools.getTile(npc.level(), pos, IWorkSite.class).map(t -> !((IWorker) npc).canWorkAt(t.getWorkType()) || !t.hasWork()).orElse(false)) {
                setMoveToNextSite();
                return;
            }
        }
        swingArm();
        doWork(entry);
    }

    private void swingArm() {
        if (ticksAtSite % 10 == 0) {
            npc.swing(InteractionHand.MAIN_HAND, true);
        }
    }

    private void doWork(WorkEntry entry) {
        if (ticksAtSite >= AWNPCStatics.npcWorkTicks) {
            ticksAtSite = 0;

            Optional<IWorkSite> workSite = WorldTools.getTile(npc.level(), entry.getPosition(), IWorkSite.class)
                    .filter(s -> ((IWorker) npc).canWorkAt(s.getWorkType()));
            if (workSite.isPresent()) {
                IWorkSite site = workSite.get();
                if (site.hasWork()) {
                    npc.addExperience(AWNPCStatics.npcXpFromWork);
                    site.addEnergyFromWorker((IWorker) npc);
                } else {
                    if (shouldMoveFromNoWork()) {
                        setMoveToNextSite();
                    }
                }
                if (shouldMoveFromTimeAtSite(entry)) {
                    setMoveToNextSite();
                }
                return;
            }
            setMoveToNextSite();
        }
    }

    private boolean shouldMoveFromNoWork() {
        return !order.getPriorityType().isTimed() && order.size() > 1;
    }

    private boolean shouldMoveFromTimeAtSite(WorkEntry entry) {
        return order.getPriorityType().isTimed() && ticksAtSite > entry.getWorkLength();
    }

    private void setMoveToNextSite() {
        ticksAtSite = 0;
        moveRetryDelay = 0;
        workIndex = order.getPriorityType().getNextWorkIndex(workIndex, order.getEntries(), npc);
    }

    public void onOrdersChanged() {
        order = WorkOrder.getWorkOrder(npc.ordersStack);
        workIndex = 0;
        ticksAtSite = 0;
    }

    @Override
    public void stop() {
        ticksAtSite = 0;
        npc.setSwingingArms(false);
        npc.removeAITask(TASK_WORK + TASK_MOVE);
    }

    public void readFromNBT(CompoundTag tag) {
        ticksAtSite = tag.getInt("ticksAtSite");
        workIndex = tag.getInt("workIndex");
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.putInt("ticksAtSite", ticksAtSite);
        tag.putInt("workIndex", workIndex);
        return tag;
    }

}
