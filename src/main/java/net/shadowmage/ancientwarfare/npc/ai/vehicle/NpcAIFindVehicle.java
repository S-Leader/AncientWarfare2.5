package net.shadowmage.ancientwarfare.npc.ai.vehicle;

import com.google.common.base.Predicate;
import net.shadowmage.ancientwarfare.npc.ai.NpcAI;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;
import net.shadowmage.ancientwarfare.npc.entity.vehicle.IVehicleUser;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;

import java.util.Comparator;
import java.util.List;

public class NpcAIFindVehicle<T extends NpcBase & IVehicleUser> extends NpcAI<T> {
    private static final double SEARCH_DISTANCE = 30D;
    @SuppressWarnings({"Guava", "java:S4738"})
    // need to use Guava Predicate because of vanilla getEntitiesWithinAABB uses it
    private static final Predicate<VehicleBase> SELECTOR = v -> v != null && v.isDrivable() && v.getPassengers().isEmpty();

    public NpcAIFindVehicle(T npc) {
        super(npc);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && !npc.getUsedVehicle().isPresent() && npc.canContinueRidingVehicle() && (!npc.isPassenger() || npc.getVehicle() instanceof VehicleBase);
    }

    @Override
    public void tick() {
        if (npc.isPassenger()) {
            //noinspection ConstantConditions
            npc.setVehicle((VehicleBase) npc.getVehicle());
            return;
        }

        List<VehicleBase> vehicles = npc.level().getEntitiesOfClass(VehicleBase.class, npc.getBoundingBox().inflate(SEARCH_DISTANCE), SELECTOR);
        vehicles.stream().filter(v -> !v.isVehicle() && v.vehicleType.canSoldiersPilot()).min(Comparator.comparing(v -> v.distanceToSqr(npc))).ifPresent(v -> npc.setVehicle(v));
    }
}
