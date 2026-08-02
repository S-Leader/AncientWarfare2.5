package net.shadowmage.ancientwarfare.npc.compat.ebwizardry;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;

/**
 * Prevents Redux spell damage between Ancient Warfare NPCs of one faction.
 */
public final class FactionAllyDesignation {
    private FactionAllyDesignation() {
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof NpcFaction target)
                || !WizardryReduxBridge.isWizardryDamage(event.getSource())) {
            return;
        }

        Entity sourceEntity = event.getSource().getEntity();
        if (sourceEntity instanceof NpcFaction caster
                && target.getFaction().equals(caster.getFaction())) {
            event.setCanceled(true);
        }
    }
}
