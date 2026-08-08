package net.shadowmage.ancientwarfare.npc.compat.ebwizardry.ai;

import com.binaris.wizardry.api.content.entity.living.ISpellCaster;
import com.binaris.wizardry.api.content.event.SpellCastEvent;
import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.spell.internal.EntityCastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.core.event.WizardryEventBus;
import com.binaris.wizardry.core.networking.s2c.NPCSpellCastS2C;
import com.binaris.wizardry.core.platform.Services;
import com.binaris.wizardry.setup.registries.Spells;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFactionSpellcasterWizardry;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Redux 1.20.1 spell attack goal adapted for AW faction NPCs.
 *
 * <p>The previous port only moved/looked at the target; baseCooldown and
 * continuousSpellDuration were unused and no Spell#cast call existed at all.
 * This restores the actual Redux casting lifecycle while retaining AW's NPC
 * hierarchy.</p>
 */
public class EntityAIAttackSpellImproved<T extends Mob & ISpellCaster> extends Goal {
    private final T attacker;
    private final int baseCooldown;
    private final int continuousSpellDuration;
    private final double speed;
    private final float maxAttackDistance;

    private LivingEntity target;
    private int cooldown = -1;
    private int continuousSpellTimer;
    private int seeTime;

    public EntityAIAttackSpellImproved(T attacker, double speed, float maxDistance, int baseCooldown, int continuousSpellDuration) {
        this.attacker = attacker;
        this.baseCooldown = baseCooldown;
        this.continuousSpellDuration = continuousSpellDuration;
        this.speed = speed;
        this.maxAttackDistance = maxDistance * maxDistance;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        LivingEntity living = attacker.getTarget();
        if (living == null || !living.isAlive()) {
            return false;
        }
        target = living;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || !attacker.getNavigation().isDone();
    }

    @Override
    public void stop() {
        target = null;
        seeTime = 0;
        cooldown = -1;
        continuousSpellTimer = 0;
        attacker.setSpellCounter(0);
        setContinuousSpellAndNotify(Spells.NONE, new SpellModifiers());
        setSpellTargetId(-1);
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            target = attacker.getTarget();
            if (target == null || !target.isAlive()) {
                return;
            }
        }

        double distanceSq = attacker.distanceToSqr(target.getX(), target.getY(), target.getZ());
        boolean targetIsVisible = attacker.getSensing().hasLineOfSight(target);

        if (targetIsVisible) {
            ++seeTime;
        } else {
            seeTime = 0;
        }

        if (distanceSq <= maxAttackDistance && seeTime >= 5) {
            attacker.getNavigation().stop();
        } else {
            attacker.getNavigation().moveTo(target, speed);
        }
        attacker.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (continuousSpellTimer > 0) {
            tickContinuousSpell(distanceSq, targetIsVisible);
            return;
        }

        if (--cooldown == 0) {
            if (distanceSq > maxAttackDistance || !targetIsVisible) {
                // Retry shortly once the target comes back into a castable state.
                cooldown = 10;
                return;
            }
            tryCastConfiguredSpell();
        } else if (cooldown < 0) {
            cooldown = baseCooldown;
        }
    }

    private void tickContinuousSpell(double distanceSq, boolean targetIsVisible) {
        --continuousSpellTimer;
        int currentTick = continuousSpellDuration - continuousSpellTimer;
        Spell spell = attacker.getContinuousSpell();
        EntityCastContext context = new EntityCastContext(attacker.level(), attacker, InteractionHand.MAIN_HAND,
                currentTick, target, attacker.getModifiers());

        attacker.setSpellCounter(currentTick);

        boolean end = distanceSq > maxAttackDistance || !targetIsVisible;
        if (!end) {
            try {
                end = WizardryEventBus.getInstance().fire(new SpellCastEvent.Tick(SpellCastEvent.Source.NPC, spell, attacker, attacker.getModifiers(), currentTick))
                        || !spell.cast(context);
            } catch (RuntimeException ex) {
                AncientWarfareNPC.LOG.error("Wizardry Redux continuous spell {} failed for AW NPC {}",
                        spell.getLocation(), attacker.getUUID(), ex);
                end = true;
            }
        }

        if (end || continuousSpellTimer == 0) {
            continuousSpellTimer = 0;
            cooldown = spell.getCooldown() + baseCooldown;
            setContinuousSpellAndNotify(Spells.NONE, new SpellModifiers());
            attacker.setSpellCounter(0);
            setSpellTargetId(-1);
            return;
        }

        if (currentTick == 1) {
            WizardryEventBus.getInstance().fire(new SpellCastEvent.Post(SpellCastEvent.Source.NPC, spell, attacker, attacker.getModifiers()));
        }
    }

    private void tryCastConfiguredSpell() {
        List<Spell> available = new ArrayList<>(attacker.getSpells());
        if (available.isEmpty() || attacker.level().isClientSide) {
            cooldown = baseCooldown;
            return;
        }

        double dx = target.getX() - attacker.getX();
        double dz = target.getZ() - attacker.getZ();

        while (!available.isEmpty()) {
            Spell spell = available.remove(attacker.level().random.nextInt(available.size()));
            if (spell == null || spell == Spells.NONE) {
                continue;
            }

            EntityCastContext context = new EntityCastContext(attacker.level(), attacker, InteractionHand.MAIN_HAND,
                    0, target, attacker.getModifiers());
            if (attemptCastSpell(spell, context)) {
                attacker.setYRot((float) (Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F);
                return;
            }
        }

        // No configured spell could be cast against this target. Do not spin
        // every tick; use the normal base delay before trying the set again.
        cooldown = baseCooldown;
    }

    private boolean attemptCastSpell(Spell spell, EntityCastContext context) {
        try {
            if (WizardryEventBus.getInstance().fire(new SpellCastEvent.Pre(SpellCastEvent.Source.NPC, spell, context.caster(), context.modifiers()))) {
                return false;
            }
            if (!spell.cast(context)) {
                return false;
            }

            if (spell.isInstantCast()) {
                WizardryEventBus.getInstance().fire(new SpellCastEvent.Post(SpellCastEvent.Source.NPC, spell, context.caster(), context.modifiers()));
                cooldown = baseCooldown + spell.getCooldown();

                if (spell.requiresPacket()) {
                    Services.NETWORK_HELPER.sendToTracking(attacker,
                            new NPCSpellCastS2C(attacker.getId(), target.getId(), InteractionHand.MAIN_HAND,
                                    spell, context.modifiers()));
                }
            } else {
                continuousSpellTimer = Math.max(1, continuousSpellDuration - 1);
                setSpellTargetId(target.getId());
                attacker.setSpellCounter(1);
                setContinuousSpellAndNotify(spell, context.modifiers());
                attacker.setTarget(target);
            }
            return true;
        } catch (RuntimeException ex) {
            // A single Redux/addon spell must not kill this NPC's whole goal.
            AncientWarfareNPC.LOG.error("Wizardry Redux spell {} failed for AW NPC {}",
                    spell.getLocation(), attacker.getUUID(), ex);
            return false;
        }
    }

    private void setContinuousSpellAndNotify(Spell spell, SpellModifiers modifiers) {
        attacker.setContinuousSpell(spell);
        if (!attacker.level().isClientSide) {
            Services.NETWORK_HELPER.sendToTracking(attacker,
                    new NPCSpellCastS2C(attacker.getId(), target == null ? -1 : target.getId(),
                            InteractionHand.MAIN_HAND, spell, modifiers));
        }
    }

    private void setSpellTargetId(int targetId) {
        if (attacker instanceof NpcFactionSpellcasterWizardry wizardryNpc) {
            wizardryNpc.setSpellTargetId(targetId);
        }
    }
}
