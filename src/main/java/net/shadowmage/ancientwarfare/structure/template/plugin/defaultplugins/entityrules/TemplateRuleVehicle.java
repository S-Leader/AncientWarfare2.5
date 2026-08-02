package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.entityrules;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

public class TemplateRuleVehicle extends TemplateRuleEntity {
    public static final String PLUGIN_NAME = "AWVehicle";

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
