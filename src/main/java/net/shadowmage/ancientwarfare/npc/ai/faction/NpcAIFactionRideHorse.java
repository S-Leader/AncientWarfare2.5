package net.shadowmage.ancientwarfare.npc.ai.faction;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.ServerLevelAccessor;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIRideHorse;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.entity.faction.IHorseMountedNpc;

public class NpcAIFactionRideHorse<T extends NpcBase & IHorseMountedNpc> extends NpcAIRideHorse<T> {
    public NpcAIFactionRideHorse(T npc) {
        super(npc, 1.5);
    }

    @Override
    protected boolean shouldRideHorse() {
        return npc.isHorseAlive() && (npc.getVehicle() == null || horse != npc.getVehicle());
    }

    @Override
    public void start() {
        if (horse == null && npc.isHorseAlive()) {
            if (npc.getVehicle() instanceof Mob) {
                horse = (Mob) npc.getVehicle();
            } else {
                spawnHorse();
            }
        } else if (horse != null && !horse.isAlive()) {
            npc.setHorseKilled();
            horse = null;
        }
    }

    private void spawnHorse() {
        Mob horse = npc.instantiateMountedEntity();
        horse.moveTo(npc.getX(), npc.getY(), npc.getZ(), npc.getYRot(), npc.getXRot());
        horse.finalizeSpawn((ServerLevelAccessor) npc.level(), npc.level().getCurrentDifficultyAt(npc.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
        if (horse instanceof AbstractHorse) {
            AbstractHorse h = (AbstractHorse) horse;
            h.setAge(0);
            h.setTamed(true);
        }

        this.horse = horse;
        npc.level().addFreshEntity(horse);
        npc.startRiding(horse);
        onMountHorse();
    }
}
