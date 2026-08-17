package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.npc.ai.*;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionHurt;

public class NpcFactionMountedSoldier extends NpcFactionMounted {
    private NpcAIAttackMeleeLongRange meleeAI;

    @SuppressWarnings("unused") //required for deserialization
    public NpcFactionMountedSoldier(EntityType<? extends PathfinderMob> type, Level world) {
        super(type, world);
        addAI();
    }

    @SuppressWarnings("unused") //used in reflection


    private void addAI() {
        meleeAI = new NpcAIAttackMeleeLongRange(this);
        tasks.addTask(0, new FloatGoal(this));
        tasks.addTask(0, new NpcAIRestrictOpenDoor(this));
        tasks.addTask(0, new NpcAIDoor(this, true));
        tasks.addTask(1, new NpcAIFollowPlayer(this));
        tasks.addTask(3, meleeAI);
        tasks.addTask(4, new NpcAIMoveHome(this, 50F, 5F, 30F, 5F));

        tasks.addTask(101, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        tasks.addTask(102, new NpcAIWander(this));
        tasks.addTask(103, new NpcAIWatchClosest(this, Mob.class, 8.0F));

        targetTasks.addTask(1, new NpcAIFactionHurt(this, this::isHostileTowards));
        targetTasks.addTask(2, new NpcAIAttackNearest(this, this::isHostileTowards));
    }

    @Override
    public String getNpcType() {
        return "cavalry";
    }

    @Override
    public void onWeaponInventoryChanged() {
        super.onWeaponInventoryChanged();

        if (meleeAI != null) {
            meleeAI.setAttackReachFromWeapon(getMainHandItem());
        }
    }
}
