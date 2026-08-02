package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.npc.ai.*;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionPanic;

public abstract class NpcFactionCivilian extends NpcFaction {

    public NpcFactionCivilian(Level world) {
        super(world);
        addAI();
    }

    public NpcFactionCivilian(Level world, String factionName) {
        super(world, factionName);
        addAI();
    }

    private void addAI() {
        tasks.addTask(0, new FloatGoal(this));
        tasks.addTask(0, new NpcAIRestrictOpenDoor(this));
        tasks.addTask(0, new NpcAIDoor(this, true));
        tasks.addTask(0, new NpcAIFactionPanic(this, 1.25D));
        tasks.addTask(1, new NpcAIFollowPlayer(this));
        tasks.addTask(2, new NpcAIMoveHome(this, 50F, 3F, 30F, 3F));

        tasks.addTask(101, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        tasks.addTask(102, new NpcAIWander(this));
        tasks.addTask(103, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    @Override
    public boolean isHostileTowards(Entity e) {
        return false;
    }

    @Override
    public boolean canTarget(Entity e) {
        return true;
    }

}
