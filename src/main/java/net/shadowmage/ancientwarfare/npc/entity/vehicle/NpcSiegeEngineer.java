package net.shadowmage.ancientwarfare.npc.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.npc.ai.*;
import net.shadowmage.ancientwarfare.npc.ai.owned.*;
import net.shadowmage.ancientwarfare.npc.ai.vehicle.*;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;
import net.shadowmage.ancientwarfare.npc.npc_command.NpcCommand;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

import javax.annotation.Nullable;
import java.util.Optional;

@SuppressWarnings({"squid:MaximumInheritanceDepth", "squid:S2160"})
public class NpcSiegeEngineer extends NpcPlayerOwned implements IVehicleUser {
    @Nullable
    private VehicleBase vehicle = null;
    private ITarget target = TargetFactory.NONE;

    @Override
    public Optional<ITarget> getVehicleTarget() {
        return target == TargetFactory.NONE ? Optional.empty() : Optional.of(target);
    }

    private void setTarget(BlockPos pos) {
        target = new BlockPosTarget(pos);
    }

    @Override
    public void resetTarget() {
        target = TargetFactory.NONE;
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

    public NpcSiegeEngineer(Level world) {
        super(world);

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new NpcAIRestrictOpenDoor(this));
        this.goalSelector.addGoal(0, new NpcAIDoor(this, true));
        this.goalSelector.addGoal(2, new NpcAIFollowPlayer(this));
        this.goalSelector.addGoal(2, new NpcAIPlayerOwnedFollowCommand(this));
        this.goalSelector.addGoal(3, new NpcAIPlayerOwnedAlarmResponse(this));
        this.goalSelector.addGoal(4, new NpcAIPlayerOwnedGetFood(this));
        this.goalSelector.addGoal(5, new NpcAIPlayerOwnedIdleWhenHungry(this));
        this.goalSelector.addGoal(6, new NpcAIDismountVehicle<>(this));
        this.goalSelector.addGoal(6, new NpcAIFindVehicle<>(this));
        this.goalSelector.addGoal(7, new NpcAIMountVehicle<>(this));
        this.goalSelector.addGoal(8, new NpcAIAimVehicle<>(this));
        this.goalSelector.addGoal(9, new NpcAIFireVehicle<>(this));

        this.goalSelector.addGoal(101, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(102, new NpcAIWander(this) {
            @Override
            public boolean canUse() {
                return !isRidingVehicle() && super.canUse();
            }
        });
        this.goalSelector.addGoal(103, new NpcAIWatchClosest(this, Mob.class, 8.0F));

        targetTasks.addTask(0, new NpcAIPlayerOwnedCommander(this));
        targetTasks.addTask(1, new NpcAIOwnerHurtByTarget(this));
        targetTasks.addTask(2, new NpcAIOwnerHurtTarget(this));
        targetTasks.addTask(3, new NpcAIHurt(this));
        targetTasks.addTask(4, new NpcAIAttackNearest(this, this::isHostileTowards));

        //1.12 applyEntityAttributes override - attributes are instance-configured in 1.20 constructors
        getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(120D);
    }

    @Override
    public String getNpcSubType() {
        return "";
    }

    @Override
    public String getNpcType() {
        return "siege_engineer";
    }

    @Override
    public boolean canBeAttackedBy(Entity e) {
        return true;
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
        return getFoodRemaining() > 0;
    }

    @Override
    public void handlePlayerCommand(NpcCommand.Command cmd) {
        if (cmd.type == NpcCommand.CommandType.ATTACK_AREA) {
            setTarget(cmd.pos);
        } else if (cmd.type == NpcCommand.CommandType.CLEAR_COMMAND) {
            resetTarget();
        } else {
            super.handlePlayerCommand(cmd);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (tickCount % 20 == 0) {
            checkTargetExistence();
        }
    }

    private void checkTargetExistence() {
        if (target != TargetFactory.NONE && !target.exists(world)) {
            target = TargetFactory.NONE;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        TargetFactory.serializeNBT(target, tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        target = TargetFactory.deserializeFromNBT(tag);
    }
}
