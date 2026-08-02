package net.shadowmage.ancientwarfare.npc.entity.faction.attributes;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.core.util.JsonUtils;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.entity.vehicle.IVehicleUser;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

import javax.annotation.Nullable;

/**
 * 1.12 loot EntityProperty ("ancientwarfarenpc:rides_vehicle") ported to the 1.20 EntitySubPredicate API.
 */
public class EntityVehicleProperty implements EntitySubPredicate {
    /**
     * Json id kept from 1.12 for the loot table entries.
     */
    public static final String NAME = AncientWarfareNPC.MOD_ID + ":rides_vehicle";
    private static final String VEHICLE_TAG = "vehicle";
    public static final EntitySubPredicate.Type TYPE = EntityVehicleProperty::deserialize;

    private final String testVehicle;

    private EntityVehicleProperty(String testVehicle) {
        this.testVehicle = testVehicle;
    }

    @Override
    public boolean matches(Entity entityIn, ServerLevel level, @Nullable Vec3 position) {
        if (entityIn instanceof IVehicleUser) {
            IVehicleUser vehicleUserEntity = (IVehicleUser) entityIn;
            if (vehicleUserEntity.isRidingVehicle() && vehicleUserEntity.getUsedVehicle().isPresent()) {
                VehicleBase vehicle = vehicleUserEntity.getUsedVehicle().get();
                String vehicleConfigName = vehicle.vehicleType.getConfigName();
                return vehicleConfigName.contains(testVehicle);
            }
        }
        return false;
    }

    @Override
    public JsonObject serializeCustomData() {
        JsonObject json = new JsonObject();
        json.addProperty(VEHICLE_TAG, testVehicle);
        return json;
    }

    @Override
    public EntitySubPredicate.Type type() {
        return TYPE;
    }

    public static EntityVehicleProperty deserialize(JsonObject json) {
        return new EntityVehicleProperty(JsonUtils.getString(json, VEHICLE_TAG));
    }
}
