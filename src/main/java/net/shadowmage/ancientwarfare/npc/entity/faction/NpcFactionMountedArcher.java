package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import com.google.common.base.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.npc.ai.*;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionArcherStayAtHome;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionHurt;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionRangedAttack;
import net.shadowmage.ancientwarfare.npc.entity.RangeAttackHelper;

public class NpcFactionMountedArcher extends NpcFactionMounted implements RangedAttackMob {
    @SuppressWarnings("unused") //used when deserializing
    public NpcFactionMountedArcher(EntityType<? extends PathfinderMob> type, Level world) {
        super(type, world);
        addAI();
    }

    @SuppressWarnings("unused") //used in reflection


    private void addAI() {
        //noinspection Guava - because dependency on what vanilla does
        Predicate<Entity> selector = entity -> {
            //noinspection ConstantConditions
            if (!isHostileTowards(entity)) {
                return false;
            }
            if (hasRestriction()) {
                BlockPos home = getRestrictCenter();
                double dist = entity.distanceToSqr(home.getX() + 0.5d, home.getY(), home.getZ() + 0.5d);
                if (dist > 30 * 30) {
                    return false;
                }
            }
            return true;
        };

        tasks.addTask(0, new FloatGoal(this));
        tasks.addTask(0, new NpcAIRestrictOpenDoor(this));
        tasks.addTask(0, new NpcAIDoor(this, true));
        tasks.addTask(1, new NpcAIFollowPlayer(this));
        tasks.addTask(2, new NpcAIFactionArcherStayAtHome(this));
        tasks.addTask(3, new NpcAIFactionRangedAttack(this));

        tasks.addTask(101, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        tasks.addTask(102, new NpcAIWander(this));
        tasks.addTask(103, new NpcAIWatchClosest(this, Mob.class, 8.0F));

        targetTasks.addTask(1, new NpcAIFactionHurt(this, selector));
        targetTasks.addTask(2, new NpcAIAttackNearest(this, selector));
    }

    @Override
    public String getNpcType() {
        return "mounted_archer";
    }

    @Override
    public void performRangedAttack(LivingEntity target, float force) {
        RangeAttackHelper.doRangedAttack(this, target, force, 1.0f);
    }
}
