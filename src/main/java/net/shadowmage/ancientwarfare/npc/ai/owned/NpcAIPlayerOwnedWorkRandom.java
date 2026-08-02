package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.shadowmage.ancientwarfare.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.NpcWorker;

import java.util.Optional;

public class NpcAIPlayerOwnedWorkRandom extends NpcAI<NpcWorker> {
    private int ticksAtSite = 0;

    public NpcAIPlayerOwnedWorkRandom(NpcWorker npc) {
        super(npc);
        setMutexBits(ATTACK + MOVE);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        if (npc.getFoodRemaining() <= 0 || npc.shouldBeAtHome()) {
            return false;
        }
        return npc.ordersStack.isEmpty() && npc.autoWorkTarget != null;
    }

    @Override
    public void start() {
        npc.addAITask(TASK_WORK);
        npc.setSwingingArms(false);
        ticksAtSite = 0;
    }

    @Override
    public void tick() {
        BlockPos pos = npc.autoWorkTarget;
        double dist = npc.getDistanceSq(pos);
        if (dist > npc.getWorkRangeSq()) {
            npc.setSwingingArms(false);
            npc.addAITask(TASK_MOVE);
            ticksAtSite = 0;
            moveToPosition(pos, dist);
        } else {
            npc.setSwingingArms(true);
            npc.getNavigation().stop();
            npc.removeAITask(TASK_MOVE);
            workAtSite();
        }
    }

    @Override
    public void stop() {
        npc.setSwingingArms(false);
        npc.removeAITask(TASK_WORK + TASK_MOVE);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    private void workAtSite() {
        ticksAtSite++;
        if (ticksAtSite % 10 == 0) {
            // Force the swing state to update locally as well as being broadcast to
            // tracking clients. Using ticksAtSite avoids Goal's reduced-tick cadence.
            npc.swing(InteractionHand.MAIN_HAND, true);
        }
        if (ticksAtSite >= AWNPCStatics.npcWorkTicks) {
            ticksAtSite = 0;
            Optional<IWorkSite> te = WorldTools.getTile(npc.level(), npc.autoWorkTarget, IWorkSite.class);
            if (te.isPresent()) {
                IWorkSite site = te.get();
                if (npc.canWorkAt(site.getWorkType()) && site.hasWork()) {
                    npc.addExperience(AWNPCStatics.npcXpFromWork);
                    site.addEnergyFromWorker(npc);
                    return;
                }
            }
            npc.autoWorkTarget = null;
        }
    }

    public void readFromNBT(CompoundTag tag) {
        ticksAtSite = tag.getInt("ticksAtSite");
    }

    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.putInt("ticksAtSite", ticksAtSite);
        return tag;
    }

}
