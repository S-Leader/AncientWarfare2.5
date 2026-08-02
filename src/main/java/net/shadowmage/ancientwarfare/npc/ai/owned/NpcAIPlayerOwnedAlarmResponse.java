package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;

public class NpcAIPlayerOwnedAlarmResponse extends NpcAI<NpcPlayerOwned> {

    public NpcAIPlayerOwnedAlarmResponse(NpcPlayerOwned npc) {
        super(npc);
        setMutexBits(ATTACK + MOVE);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && npc.getUpkeepPoint().isPresent() && npc.getUpkeepDimensionId() == getLegacyDimensionId(npc.level()) && npc.isAlarmed;
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
        npc.addAITask(TASK_ALARM);
    }

    @Override
    public void tick() {
        npc.getUpkeepPoint().ifPresent(pos -> {
            double dist = npc.distanceToSqr(pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d);
            if (dist > AWNPCStatics.npcActionRange * AWNPCStatics.npcActionRange) {
                npc.addAITask(TASK_MOVE);
                moveToPosition(pos, dist);
            } else {
                npc.removeAITask(TASK_MOVE);
            }
        });
    }

    @Override
    public void stop() {
        moveRetryDelay = 0;
        npc.removeAITask(TASK_ALARM + TASK_MOVE);
    }
}
