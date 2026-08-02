package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.npc.ai.*;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionArcherStayAtHome;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionHurt;

/* Spellcaster NPC class for the base spellcaster NPCs, casting some basic AW spells.
 * This class is only used if EBWizardry is not present. Cannot have any reference to EBWizardry objects. */
public class NpcFactionSpellcaster extends NpcFaction {

    @SuppressWarnings("unused")
    public NpcFactionSpellcaster(Level world) {
        super(world);
        addAI();
    }

    @SuppressWarnings("unused")
    public NpcFactionSpellcaster(Level world, String factionName) {
        super(world, factionName);
        addAI();
    }

    @SuppressWarnings("Duplicates")
    private void addAI() {
        tasks.addTask(0, new FloatGoal(this));
        tasks.addTask(0, new NpcAIRestrictOpenDoor(this));
        tasks.addTask(0, new NpcAIDoor(this, true));
        tasks.addTask(1, new NpcAIFollowPlayer(this));
        tasks.addTask(2, new NpcAIMoveHome(this, 50.f, 5.f, 30.f, 5.f));
        tasks.addTask(2, new NpcAIFactionArcherStayAtHome(this));

        tasks.addTask(101, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        tasks.addTask(102, new NpcAIWander(this));
        tasks.addTask(103, new NpcAIWatchClosest(this, Mob.class, 8.0F));

        targetTasks.addTask(1, new NpcAIFactionHurt(this, this::isHostileTowards));
        targetTasks.addTask(2, new NpcAIAttackNearest(this, this::isHostileTowards));
    }

    @Override
    public boolean canAttackClass(Class claz) {
        return true;
    }

    @Override
    public boolean worksInRain() {
        return true;
    }

    @Override
    public boolean isPassive() {
        return false;
    }

    @Override
    public String getNpcType() {
        return "spellcaster";
    }

    @Override
    public boolean shouldSleep() {
        return false;
    }
}
