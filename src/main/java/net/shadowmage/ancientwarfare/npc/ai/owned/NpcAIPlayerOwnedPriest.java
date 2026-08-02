package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;
import net.shadowmage.ancientwarfare.npc.item.ItemNpcSpawner;
import net.shadowmage.ancientwarfare.npc.tile.TileTownHall;
import net.shadowmage.ancientwarfare.npc.tile.TileTownHall.NpcDeathEntry;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static net.shadowmage.ancientwarfare.npc.config.AWNPCStatics.npcKeepEquipment;

public class NpcAIPlayerOwnedPriest extends NpcAI<NpcPlayerOwned> {

    private static final int UPDATE_FREQ = 200;
    private static final int RESURRECTION_TIME = 100;
    private int lastCheckTicks = -1;
    private NpcDeathEntry entryToRes;
    private int resurrectionDelay = 0;

    public NpcAIPlayerOwnedPriest(NpcPlayerOwned npc) {
        super(npc);
        setMutexBits(ATTACK + MOVE);
    }

    @Override
    public boolean canUse() {
        if (!super.canUse()) {
            return false;
        }
        if (entryToRes == null) {
            return (lastCheckTicks == -1 || npc.tickCount - lastCheckTicks > UPDATE_FREQ) && npc.getTownHall().map(t -> !t.getDeathList().isEmpty()).orElse(false);
        }
        return npc.getTownHall().isPresent() && !entryToRes.resurrected && entryToRes.beingResurrected;
    }

    @Override
    public void start() {
        lastCheckTicks = npc.tickCount;
        List<NpcDeathEntry> list = npc.getTownHall().map(TileTownHall::getDeathList).orElse(Collections.emptyList());
        for (NpcDeathEntry entry : list) {
            if (entry.canRes && !entry.resurrected && !entry.beingResurrected) {
                entryToRes = entry;
                entry.beingResurrected = true;
                break;
            }
        }
    }

    @Override
    public void tick() {
        if (entryToRes == null || entryToRes.resurrected) {
            return;
        }
        Optional<BlockPos> townHallPos = npc.getTownHallPosition();
        if (!townHallPos.isPresent()) {
            return;
        }
        BlockPos pos = townHallPos.get();
        double dist = npc.distanceToSqr(pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d);
        if (dist > AWNPCStatics.npcActionRange * AWNPCStatics.npcActionRange) {
            moveToPosition(pos, dist);
            resurrectionDelay = 0;
        } else {
            resurrectionDelay++;
            npc.swing(InteractionHand.MAIN_HAND);
            if (resurrectionDelay > RESURRECTION_TIME) {
                resurrectionDelay = 0;
                resurrectTarget();
            }
        }
    }

    private void resurrectTarget() {
        NpcBase resdNpc = ItemNpcSpawner.createNpcFromItem(npc.level(), entryToRes.stackToSpawn);
        entryToRes.beingResurrected = false;
        if (resdNpc != null) {
            if (!npcKeepEquipment) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    resdNpc.setItemSlot(slot, ItemStack.EMPTY);
                }
                resdNpc.setShieldStack(ItemStack.EMPTY);
            }
            resdNpc.setOwner(npc.getOwner());
            resdNpc.setHealth(resdNpc.getMaxHealth() / 2);
            resdNpc.moveTo(npc.getX(), npc.getY(), npc.getZ(), npc.getYRot(), npc.getXRot());
            resdNpc.knockback(0, 2 * npc.getRandom().nextDouble() - 1, 2 * npc.getRandom().nextDouble() - 1);
            resdNpc.setDeltaMovement(resdNpc.getDeltaMovement().multiply(1, 0, 1));
            entryToRes.resurrected = npc.level().addFreshEntity(resdNpc);
        }
        npc.getTownHall().ifPresent(TileTownHall::informViewers);
        entryToRes = null;
    }

    @Override
    public void stop() {
        if (entryToRes != null && !entryToRes.resurrected) {
            entryToRes.beingResurrected = false;
        }
        entryToRes = null;
    }

}
