package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.entityrules;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.datafixes.ComponentItemFixer;
import net.shadowmage.ancientwarfare.core.entity.AWEntityRegistry;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;
import net.shadowmage.ancientwarfare.vehicle.entity.IVehicleType;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.entity.types.VehicleType;

public class TemplateRuleVehicle extends TemplateRuleEntity {
    public static final String PLUGIN_NAME = "AWVehicle";
    private static final ResourceLocation VEHICLE_ENTITY_ID =
            new ResourceLocation(AncientWarfareVehicles.MOD_ID, AWEntityRegistry.VEHICLE);

    private float turretRotation;

    public TemplateRuleVehicle() {
        super();
    }

    public TemplateRuleVehicle(Level world, Entity entity, int turns, int x, int y, int z) {
        super(world, entity, turns, x, y, z);
        rotation = (entity.getYRot() - 90.f * turns) % 360.f;
        turretRotation = (((VehicleBase) entity).localTurretDestRot - 90.f * turns) % 360.f;
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        CompoundTag vehicleNbt;
        try {
            vehicleNbt = ComponentItemFixer.fixRecursively(getEntityNBT(pos, turns).copy());
        } catch (RuntimeException | LinkageError exception) {
            AncientWarfareStructure.LOG.error("Unable to data-fix AW vehicle NBT in structure at {}; vehicle skipped",
                    pos, exception);
            return;
        }

        removeCopiedEntityIdentity(vehicleNbt);

        int vehicleTypeId = vehicleNbt.getInt("vehType");
        IVehicleType vehicleType = VehicleType.getVehicleType(vehicleTypeId);
        if (vehicleType == null) {
            AncientWarfareStructure.LOG.error(
                    "Skipping invalid AW vehicle type {} in structure at {}", vehicleTypeId, pos);
            return;
        }

        int materialLevel = clampMaterialLevel(vehicleType, vehicleNbt.getInt("matLvl"));
        vehicleNbt.putInt("vehType", vehicleTypeId);
        vehicleNbt.putInt("matLvl", materialLevel);

        /*
         * Do NOT use VehicleType.getVehicleForType here. That method is the item/
         * gameplay factory and deliberately refuses disabled vehicle types. The old
         * 1.12 structure rule did not apply that crafting/config gate: an AWS file
         * that contains a ballista/cannon should still reproduce the saved entity.
         */
        VehicleBase vehicle = createRawVehicle(world);
        if (vehicle == null) {
            AncientWarfareStructure.LOG.error(
                    "Unable to create registered AW vehicle entity for type {} at {}", vehicleTypeId, pos);
            return;
        }

        boolean restored = restoreLegacyVehicleState(vehicle, vehicleNbt, vehicleType, materialLevel, pos);
        if (!restored) {
            /*
             * Legacy 1.12 templates may contain obsolete ForgeCaps/inventory/ammo
             * payloads. A bad optional state blob must not make the engineering
             * vehicle disappear. Recreate a clean vehicle and at least preserve its
             * type/material/health plus the primitive aiming values that are safe in
             * every AW2 template version.
             */
            vehicle = createRawVehicle(world);
            if (vehicle == null) {
                return;
            }
            initializeCleanVehicle(vehicle, vehicleType, materialLevel, vehicleNbt);
            AncientWarfareStructure.LOG.warn(
                    "Spawned clean AW vehicle type {} at {} because its legacy saved state could not be restored",
                    vehicleTypeId, pos);
        }

        try {
            updateEntityOnPlacement(turns, pos, vehicle);
            if (!world.addFreshEntity(vehicle)) {
                AncientWarfareStructure.LOG.error(
                        "AW vehicle type {} was created but ServerLevel rejected it at {}", vehicleTypeId, pos);
            }
        } catch (RuntimeException | LinkageError exception) {
            AncientWarfareStructure.LOG.error(
                    "Failed to add AW vehicle type {} from town/structure template at {}",
                    vehicleTypeId, pos, exception);
        }
    }

    private VehicleBase createRawVehicle(Level world) {
        return AWEntityRegistry.createEntity(VEHICLE_ENTITY_ID, world, VehicleBase.class);
    }

    private boolean restoreLegacyVehicleState(VehicleBase vehicle, CompoundTag vehicleNbt,
                                              IVehicleType vehicleType, int materialLevel, BlockPos pos) {
        try {
            vehicle.load(vehicleNbt);

            /*
             * The template's vehType/matLvl are authoritative. Reapply them after
             * Entity#load so malformed/partially migrated NBT cannot leave the entity
             * on VehicleBase's dummy catapult type.
             */
            if (vehicle.vehicleType != vehicleType || vehicle.vehicleMaterialLevel != materialLevel) {
                vehicle.setVehicleType(vehicleType, materialLevel);
            }
            if (vehicle.getHealth() <= 0.0F) {
                vehicle.setInitialHealth();
            }
            return true;
        } catch (RuntimeException | LinkageError exception) {
            AncientWarfareStructure.LOG.warn(
                    "Legacy state restore failed for AW vehicle type {} at {}; falling back to clean vehicle",
                    vehicleNbt.getInt("vehType"), pos, exception);
            return false;
        }
    }

    private void initializeCleanVehicle(VehicleBase vehicle, IVehicleType vehicleType,
                                        int materialLevel, CompoundTag vehicleNbt) {
        vehicle.setVehicleType(vehicleType, materialLevel);
        vehicle.setInitialHealth();

        if (vehicleNbt.contains("health")) {
            vehicle.setHealth(vehicleNbt.getFloat("health"));
        }
        if (vehicleNbt.contains("turHome")) {
            vehicle.localTurretRotationHome = vehicleNbt.getFloat("turHome");
        }
        if (vehicleNbt.contains("lc")) {
            vehicle.localLaunchPower = vehicleNbt.getFloat("lc");
        }
        if (vehicleNbt.contains("tp")) {
            vehicle.localTurretPitch = vehicleNbt.getFloat("tp");
        }
        if (vehicleNbt.contains("tpd")) {
            vehicle.localTurretDestPitch = vehicleNbt.getFloat("tpd");
        }
    }

    private int clampMaterialLevel(IVehicleType vehicleType, int materialLevel) {
        if (vehicleType.getMaterialType() == null) {
            return Math.max(0, materialLevel);
        }
        int levels = Math.max(1, vehicleType.getMaterialType().getNumOfLevels());
        return Math.max(0, Math.min(materialLevel, levels - 1));
    }

    private void removeCopiedEntityIdentity(CompoundTag vehicleNbt) {
        // Modern Entity#load understands UUID as an int-array, while old templates
        // may also carry UUIDMost/UUIDLeast. None of them should be reused.
        vehicleNbt.remove("UUID");
        vehicleNbt.remove("UUIDMost");
        vehicleNbt.remove("UUIDLeast");
    }

    @Override
    protected void updateEntityOnPlacement(int turns, BlockPos pos, Entity e) {
        e.moveTo(pos.getX() + BlockTools.rotateFloatX(xOffset, zOffset, turns), pos.getY() + yOffset,
                pos.getZ() + BlockTools.rotateFloatZ(xOffset, zOffset, turns), (rotation - 90f * turns) % 360f, 0);

        ((VehicleBase) e).localTurretRotation = (turretRotation - 90f * turns) % 360;
        ((VehicleBase) e).localTurretDestRot = ((VehicleBase) e).localTurretRotation;
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        super.writeRuleData(tag);
        tag.putFloat("turretRotation", turretRotation);
    }

    @Override
    public void parseRule(CompoundTag tag) {
        super.parseRule(tag);
        turretRotation = tag.getFloat("turretRotation");
    }
}
