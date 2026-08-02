package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.npc.ai.*;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionHurt;
import net.shadowmage.ancientwarfare.npc.ai.vehicle.NpcAIAimVehicle;
import net.shadowmage.ancientwarfare.npc.ai.vehicle.NpcAIFindVehicle;
import net.shadowmage.ancientwarfare.npc.ai.vehicle.NpcAIFireVehicle;
import net.shadowmage.ancientwarfare.npc.ai.vehicle.NpcAIMountVehicle;
import net.shadowmage.ancientwarfare.npc.entity.vehicle.EntityTarget;
import net.shadowmage.ancientwarfare.npc.entity.vehicle.ITarget;
import net.shadowmage.ancientwarfare.npc.entity.vehicle.IVehicleUser;
import net.shadowmage.ancientwarfare.npc.entity.vehicle.TargetFactory;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

import javax.annotation.Nullable;
import java.util.Optional;

@SuppressWarnings("squid:S2160")
public class NpcFactionSiegeEngineer extends NpcFaction implements IVehicleUser {

    private VehicleBase vehicle = null;
    private ITarget target = TargetFactory.NONE;

    @SuppressWarnings("unused")
    public NpcFactionSiegeEngineer(Level world) {
        super(world);
        addAI();
    }

    @SuppressWarnings("unused")
    public NpcFactionSiegeEngineer(Level world, String factionName) {
        super(world, factionName);
        addAI();
    }

    private void addAI() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new NpcAIRestrictOpenDoor(this));
        this.goalSelector.addGoal(0, new NpcAIDoor(this, true));
        this.goalSelector.addGoal(1, new NpcAIFollowPlayer(this));
        this.goalSelector.addGoal(2, new NpcAIMoveHome(this, 50F, 5F, 30F, 5F));
        this.goalSelector.addGoal(3, new NpcAIFindVehicle<>(this));
        this.goalSelector.addGoal(4, new NpcAIMountVehicle<>(this));
        this.goalSelector.addGoal(5, new NpcAIAimVehicle<>(this));
        this.goalSelector.addGoal(6, new NpcAIFireVehicle<>(this));

        this.goalSelector.addGoal(101, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(102, new NpcAIWander(this));
        this.goalSelector.addGoal(103, new NpcAIWatchClosest(this, Mob.class, 8.0F));

        this.targetSelector.addGoal(1, new NpcAIFactionHurt(this, this::isHostileTowards));
        this.targetSelector.addGoal(2, new NpcAIAttackNearest(this, this::isHostileTowards));
    }

    @Override
    public String getNpcType() {
        return "siege_engineer";
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
    public boolean shouldSleep() {
        return false;
    }

    @Override
    public void setTarget(@Nullable LivingEntity entity) {
        super.setTarget(entity);
        if (entity != null) {
            target = new EntityTarget(entity);
        } else {
            resetTarget();
        }
    }

    @Override
    public void resetTarget() {
        target = TargetFactory.NONE;
    }

    @Override
    public Optional<VehicleBase> getUsedVehicle() {
        if (vehicle == null || vehicle.isRemoved()) {
            return Optional.empty();
        }
        return Optional.of(vehicle);
    }

    @Override
    public void setVehicle(VehicleBase vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public void resetVehicle() {
        vehicle = null;
    }

    @Override
    public boolean isRidingVehicle() {
        return getUsedVehicle().isPresent() && isPassenger();
    }

    @Override
    public boolean canContinueRidingVehicle() {
        return true;
    }

    @Override
    public Optional<ITarget> getVehicleTarget() {
        return target == TargetFactory.NONE ? Optional.empty() : Optional.of(target);
    }
}
