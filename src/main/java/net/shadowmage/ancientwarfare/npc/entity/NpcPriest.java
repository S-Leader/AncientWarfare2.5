package net.shadowmage.ancientwarfare.npc.entity;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.npc.ai.*;
import net.shadowmage.ancientwarfare.npc.ai.owned.*;

public class NpcPriest extends NpcPlayerOwned {

    public NpcPriest(EntityType<? extends PathfinderMob> type, Level par1World) {
        super(type, par1World);

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new NpcAIRestrictOpenDoor(this));
        this.goalSelector.addGoal(0, new NpcAIDoor(this, true));
        this.goalSelector.addGoal(0, (horseAI = new NpcAIPlayerOwnedRideHorse(this)));
        this.goalSelector.addGoal(2, new NpcAIFollowPlayer(this));
        this.goalSelector.addGoal(2, new NpcAIPlayerOwnedFollowCommand(this));
        this.goalSelector.addGoal(3, new NpcAIFleeHostiles(this));
        this.goalSelector.addGoal(3, new NpcAIPlayerOwnedAlarmResponse(this));
        this.goalSelector.addGoal(4, new NpcAIPlayerOwnedGetFood(this));
        this.goalSelector.addGoal(5, new NpcAIPlayerOwnedIdleWhenHungry(this));
        this.goalSelector.addGoal(6, new NpcAIMoveHome(this, 50F, 8F, 30F, 3F));
        this.goalSelector.addGoal(7, new NpcAIPlayerOwnedPriest(this));

        //post-100 -- used by delayed shared tasks (look at random stuff, wander)
        this.goalSelector.addGoal(101, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(102, new NpcAIWander(this));
        this.goalSelector.addGoal(103, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    @Override
    public void onOrdersInventoryChanged() {

    }

    @Override
    public String getNpcSubType() {
        return "";
    }

    @Override
    public String getNpcType() {
        return "priest";
    }

}
