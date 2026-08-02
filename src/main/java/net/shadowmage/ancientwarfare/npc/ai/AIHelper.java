package net.shadowmage.ancientwarfare.npc.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

public final class AIHelper {
    private AIHelper() {
    }

    private static Set<Integer> additionalHostileEntitiesToTarget = new HashSet<>();

    public static boolean isTarget(NpcBase npc, @Nullable LivingEntity target, boolean checkSight) {
        return target != null && !(target == npc || !target.isAlive() || !npc.canTarget(target)
                || target instanceof Player && ((Player) target).getAbilities().invulnerable
                || checkSight && !npc.getSensing().hasLineOfSight(target));
    }

    public static boolean isWithinFollowRange(Mob entity, LivingEntity target) {
        return entity.distanceTo(target) <= entity.getAttribute(Attributes.FOLLOW_RANGE).getValue();
    }

    public static void addHostileEntityToTarget(Entity e) {
        additionalHostileEntitiesToTarget.add(e.getId());
    }

    public static void removeHostileEntityToTarget(Entity e) {
        additionalHostileEntitiesToTarget.remove(e.getId());
    }

    public static boolean isAdditionalEntityToTarget(Entity e) {
        return additionalHostileEntitiesToTarget.contains(e.getId());
    }
}
