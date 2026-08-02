package net.shadowmage.ancientwarfare.vehicle.VehicleVarHelpers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.entity.EntityGate;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.entity.types.VehicleTypeBatteringRam;
import net.shadowmage.ancientwarfare.vehicle.helpers.VehicleFiringVarsHelper;
import net.shadowmage.ancientwarfare.vehicle.init.AWVehicleSounds;
import net.shadowmage.ancientwarfare.vehicle.missiles.DamageType;

import java.util.List;

public class BatteringRamVarHelper extends VehicleFiringVarsHelper {

    float logAngle = 0.f;
    float logSpeed = 0.f;

    /**
     * @param vehicle
     */
    public BatteringRamVarHelper(VehicleBase vehicle) {
        super(vehicle);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("lA", logAngle);
        tag.putFloat("lS", logSpeed);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        logAngle = tag.getFloat("lA");
        logSpeed = tag.getFloat("lS");
    }

    @Override
    public void onFiringUpdate() {
        if (logAngle >= 30) {
            vehicle.firingHelper.startLaunching();
            logSpeed = 0;
        } else {
            logAngle++;
            logSpeed = 1;
        }
    }

    @Override
    public void onReloadUpdate() {
        if (logAngle < 0) {
            logAngle++;
            logSpeed = 1;
        } else {
            logAngle = 0;
            logSpeed = 0;
        }
    }

    @Override
    public void onLaunchingUpdate() {
        if (logAngle <= -30) {
            vehicle.firingHelper.setFinishedLaunching();
            doDamageEffects();
            logSpeed = 0;
        } else {
            logAngle -= 2;
            logSpeed = -2;
        }
    }

    public void doDamageEffects() {
        if (vehicle.level().isClientSide) {
            return;
        }
        BlockPos[] effectedPositions = VehicleTypeBatteringRam.getEffectedPositions(vehicle);
        AABB bb;
        List<Entity> hitEntities;
        boolean gateSoundPlayed = false;
        for (BlockPos pos : effectedPositions) {
            if (pos == null) {
                continue;
            }
            bb = new AABB(pos, pos.offset(1, 1, 1));
            hitEntities = vehicle.level().getEntities(vehicle, bb);
            if (hitEntities != null) {
                for (Entity ent : hitEntities) {
                    ent.hurt(DamageType.batteringDamage, AWCoreStatics.batteringRamBaseDamage + vehicle.vehicleMaterialLevel);
                    if (ent instanceof EntityGate && !gateSoundPlayed) {
                        String gateTypeName = (((EntityGate) ent).gateType.getVariant().toString().toLowerCase());
                        if (gateTypeName.contains("wood")) {
                            ent.playSound(AWVehicleSounds.BATTERING_RAM_HIT_WOOD, 3, 1);
                            gateSoundPlayed = true;
                        } else if (gateTypeName.contains("iron")) {
                            ent.playSound(AWVehicleSounds.BATTERING_RAM_HIT_IRON, 3, 1);
                            gateSoundPlayed = true;
                        } else {
                            ent.playSound(AWVehicleSounds.BATTERING_RAM_HIT_STONE, 3, 1);
                            gateSoundPlayed = true;
                        }
                    }
                }
            }
            if (AWVehicleStatics.generalSettings.batteringRamBreaksBlocks) {
                if (vehicle.level().random.nextDouble() < AWVehicleStatics.generalSettings.batteringRamBlockBreakPercentageChance / 100.0D) {
                    BlockTools.breakBlockAndDrop(vehicle.level(), pos);
                }
            }
        }
    }

    @Override
    public void onReloadingFinished() {
        logAngle = 0;
        logSpeed = 0;
    }

    @Override
    public float getVar1() {
        return logAngle;
    }

    @Override
    public float getVar2() {
        return logSpeed;
    }

    @Override
    public float getVar3() {
        return 0;
    }

    @Override
    public float getVar4() {
        return 0;
    }

    @Override
    public float getVar5() {
        return 0;
    }

    @Override
    public float getVar6() {
        return 0;
    }

    @Override
    public float getVar7() {
        return 0;
    }

    @Override
    public float getVar8() {
        return 0;
    }

}
