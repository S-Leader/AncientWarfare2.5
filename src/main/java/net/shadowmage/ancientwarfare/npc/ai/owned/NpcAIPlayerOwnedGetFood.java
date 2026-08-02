package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;

public class NpcAIPlayerOwnedGetFood extends NpcAI<NpcPlayerOwned> {
    public NpcAIPlayerOwnedGetFood(NpcPlayerOwned npc) {
        super(npc);
        setMutexBits(MOVE + ATTACK + HUNGRY);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        return npc.requiresUpkeep() && npc.getUpkeepPoint().isPresent()
                && (npc.getFoodRemaining() == 0 || (isGettingFood() && npc.getFoodRemaining() < npc.getUpkeepAmount()))
                && npc.getUpkeepDimensionId() == getLegacyDimensionId(npc.level());
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

    private boolean isGettingFood() {
        return (npc.getAITasks() & TASK_UPKEEP) != 0;
    }

    /*
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void start() {
        npc.addAITask(TASK_UPKEEP);
    }

    /*
     * Updates the task
     */
    @Override
    public void tick() {
        npc.getUpkeepPoint().ifPresent(pos -> {
            double dist = npc.distanceToSqr(pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d);
            if (dist > AWNPCStatics.npcActionRange * AWNPCStatics.npcActionRange) {
                npc.addAITask(TASK_MOVE);
                moveToPosition(pos, dist);
            } else {
                npc.removeAITask(TASK_MOVE);
                tryUpkeep(pos);
            }
        });
    }

    /*
     * Resets the task
     */
    @Override
    public void stop() {
        moveRetryDelay = 0;
        npc.removeAITask(TASK_UPKEEP + TASK_MOVE);
    }

    private void tryUpkeep(BlockPos pos) {
        WorldTools.getItemHandlerFromTile(npc.level(), pos, npc.getUpkeepBlockSide()).ifPresent(h -> npc.withdrawFood(h));
    }
}