package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.shadowmage.ancientwarfare.core.interfaces.IWorkSite;
import net.shadowmage.ancientwarfare.core.owner.Owner;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.entity.NpcWorker;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class NpcAIPlayerOwnedFindWorksite extends NpcAI<NpcWorker> {

    private int lastExecuted = -1;//set to -1 default to trigger should execute lookup on first run
    private static final int CHECK_FREQUENCY = 200;//how often to recheck if orders and work target are both null
    private static final int RANGE = 40;

    public NpcAIPlayerOwnedFindWorksite(NpcWorker npc) {
        super(npc);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        return (lastExecuted == -1 || npc.tickCount - lastExecuted > CHECK_FREQUENCY) && npc.ordersStack.isEmpty() && npc.autoWorkTarget == null;
    }

    @Override
    public void start() {
        lastExecuted = npc.tickCount;
        if (npc.autoWorkTarget != null)//validate existing position
        {
            BlockPos pos = npc.autoWorkTarget;
            Optional<IWorkSite> te = WorldTools.getTile(npc.level(), pos, IWorkSite.class);
            if (te.isPresent()) {
                IWorkSite site = te.get();
                if (!npc.canWorkAt(site.getWorkType()) || npc.hasCommandPermissions(site.getOwner()) || !site.hasWork()) {
                    npc.autoWorkTarget = null;
                }
            } else {
                npc.autoWorkTarget = null;
            }
        }
        if (npc.autoWorkTarget == null) {
            findWorkTarget();
        }
    }

    private void findWorkTarget() {
        int x = Mth.floor(npc.getX());
        int y = Mth.floor(npc.getY());
        int z = Mth.floor(npc.getZ());
        List<BlockEntity> tiles = WorldTools.getTileEntitiesInArea(npc.level(), x - RANGE, y - RANGE / 2, z - RANGE, x + RANGE, y + RANGE / 2, z + RANGE);
        if (tiles.isEmpty()) {
            return;
        }
        npc.autoWorkTarget = getClosestWorksitePos(tiles);
    }

    @Nullable
    private BlockPos getClosestWorksitePos(List<BlockEntity> tiles) {
        BlockPos closestPos = null;
        double closestDist = -1;
        for (BlockEntity te : tiles) {
            if (te instanceof IWorkSite) {
                IWorkSite site = (IWorkSite) te;
                if (site.getOwner() != Owner.EMPTY && !npc.hasCommandPermissions(site.getOwner())) {
                    continue;
                }
                if (npc.canWorkAt(site.getWorkType()) && site.hasWork()) {
                    double dist = npc.distanceToSqr(te.getBlockPos().getX() + 0.5d, te.getBlockPos().getY(), te.getBlockPos().getZ() + 0.5d);
                    if (closestDist == -1 || dist < closestDist) {
                        closestDist = dist;
                        closestPos = te.getBlockPos();
                    }
                }
            }
        }
        return closestPos;
    }

}
