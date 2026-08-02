package net.shadowmage.ancientwarfare.vehicle.config;

import net.shadowmage.ancientwarfare.core.config.ModConfiguration;

public class AWVehicleStatics extends ModConfiguration {

    public static final String KEY_VEHICLE_FORWARD = "keybind.vehicle.forward";
    public static final String KEY_VEHICLE_REVERSE = "keybind.vehicle.reverse";
    public static final String KEY_VEHICLE_LEFT = "keybind.vehicle.left";
    public static final String KEY_VEHICLE_RIGHT = "keybind.vehicle.right";
    public static final String KEY_VEHICLE_FIRE = "keybind.vehicle.fire";
    public static final String KEY_VEHICLE_ASCEND_AIM_UP = "keybind.vehicle.ascend.aim.up";
    public static final String KEY_VEHICLE_DESCEND_AIM_DOWN = "keybind.vehicle.descend.aim.down";
    public static final String KEY_VEHICLE_AMMO_PREV = "keybind.vehicle.ammo.prev";
    public static final String KEY_VEHICLE_AMMO_NEXT = "keybind.vehicle.ammo.next";
    public static final String KEY_VEHICLE_TURRET_LEFT = "keybind.vehicle.turret.left";
    public static final String KEY_VEHICLE_TURRET_RIGHT = "keybind.vehicle.turret.right";
    public static final String KEY_VEHICLE_MOUSE_AIM = "keybind.vehicle.mouse.aim";
    public static final String KEY_VEHICLE_AMMO_SELECT = "keybind.vehicle.ammo.select";

    public static final String ClientAndServerSideNote = "Affect both client and server. These configs must match for client and server, or strange and probably BAD things WILL happen.";

    public AWVehicleStatics(String mod) {
        super(mod);
    }

    public static GeneralSettings generalSettings = new GeneralSettings();

    public static class GeneralSettings {

        public boolean oversizeAmmoEnabled = true;

        public boolean ownedSoldiersUseAmmo = true;

        public boolean vehiclesTearUpGrass = true;

        public boolean shotsDestroysBlocks = true;

        public boolean batteringRamBreaksBlocks = true;

        public int batteringRamBlockBreakPercentageChance = 20;
        public boolean blockFires = true;

        public boolean allowFriendlyFire = false;

        public boolean useVehicleSetupTime = true;

        public int assignedRiderSearchRange = 16;
    }

    public static ClientSettings clientSettings = new ClientSettings();

    public static class ClientSettings {
        public boolean adjustMissilesForAccuracy = true;

        public boolean renderVehiclesInFirstPerson = true;

        public boolean renderVehicleNameplates = true;

        public boolean renderVehicleNameplateHealth = true;

        public boolean renderOverlay = true;

        public boolean renderAdvOverlay = true;

        public boolean enableMouseAim = true;
    }

    public static VehicleStats vehicleStats = new VehicleStats();

    public static class VehicleStats {
        public int ammoBallistaBoltDamage = 18;

        public int ammoBallistaBoltIronDamage = 30;

        public int ammoBallistaBoltFlameDamage = 16;

        public int ammoBallistaBoltExplosiveDamage = 15;

        public int ammoCannonBall5kgDamage = 10;

        public int ammoCannonBall10kgDamage = 15;

        public int ammoCannonBall15kgDamage = 30;

        public int ammoCannonBall25kgDamage = 45;

        public int ammoCanisterDamage = 8;

        public int ammoHwachaRocketDamage = 6;

        public int ammoHwachaRocketFlameDamage = 5;

        public int ammoHwachaRocketExplosiveDamage = 4;

        public int vehicleBallistaHealth = 100;

        public int vehicleBallistaBoatHealth = 100;

        public int vehicleBatteringRamHealth = 100;

        public int vehicleCatapultHealth = 100;

        public int vehicleCatapultBoatHealth = 100;

        public int vehicleCannonHealth = 100;

        public int vehicleChestCartHealth = 100;

        public int vehicleChestBoatHealth = 100;

        public int vehicleHwachaHealth = 100;

        public int vehicleTrebuchetHealth = 100;

        public int vehicleGiantTrebuchetHealth = 175;
    }

    @Override
    public void initializeCategories() {
        config.addCustomCategoryComment(generalOptions, "Vehicle gameplay settings. " + ClientAndServerSideNote);
        config.addCustomCategoryComment(clientOptions, "Vehicle rendering and client-only settings.");
        config.addCustomCategoryComment(tweakOptions, "Vehicle ammunition damage and maximum health settings. " + ClientAndServerSideNote);
    }

    @Override
    public void initializeValues() {
        generalSettings.oversizeAmmoEnabled = bool(generalOptions, "oversize_ammo_enabled", generalSettings.oversizeAmmoEnabled, "Allow oversized ammunition in regular vehicles.");
        generalSettings.ownedSoldiersUseAmmo = bool(generalOptions, "owned_soldiers_use_ammo", generalSettings.ownedSoldiersUseAmmo, "Require player-owned soldiers to consume vehicle ammunition.");
        generalSettings.vehiclesTearUpGrass = bool(generalOptions, "vehicles_tear_up_grass", generalSettings.vehiclesTearUpGrass, "Moving vehicles turn grass into dirt.");
        generalSettings.shotsDestroysBlocks = bool(generalOptions, "shots_destroy_blocks", generalSettings.shotsDestroysBlocks, "Vehicle projectiles may destroy blocks.");
        generalSettings.batteringRamBreaksBlocks = bool(generalOptions, "battering_ram_breaks_blocks", generalSettings.batteringRamBreaksBlocks, "Battering rams may break blocks.");
        generalSettings.batteringRamBlockBreakPercentageChance = integer(generalOptions, "battering_ram_block_break_percentage_chance", generalSettings.batteringRamBlockBreakPercentageChance, 1, 100, "Per-block break chance for battering rams.");
        generalSettings.blockFires = bool(generalOptions, "block_fires", generalSettings.blockFires, "Fire and explosive shots may ignite nearby blocks.");
        generalSettings.allowFriendlyFire = bool(generalOptions, "allow_friendly_fire", generalSettings.allowFriendlyFire, "Vehicle shots may damage allied NPCs and players.");
        generalSettings.useVehicleSetupTime = bool(generalOptions, "use_vehicle_setup_time", generalSettings.useVehicleSetupTime, "Vehicles require setup time after placement.");
        generalSettings.assignedRiderSearchRange = integer(generalOptions, "assigned_rider_search_range", generalSettings.assignedRiderSearchRange, 3, 128, "Maximum distance before a vehicle releases its assigned NPC rider.");

        clientSettings.adjustMissilesForAccuracy = bool(clientOptions, "adjust_missiles_for_accuracy", clientSettings.adjustMissilesForAccuracy, "Apply client missile accuracy adjustment.");
        clientSettings.renderVehiclesInFirstPerson = bool(clientOptions, "render_vehicles_in_first_person", clientSettings.renderVehiclesInFirstPerson, "Render a ridden vehicle in first-person view.");
        clientSettings.renderVehicleNameplates = bool(clientOptions, "render_vehicle_nameplates", clientSettings.renderVehicleNameplates, "Render vehicle nameplates.");
        clientSettings.renderVehicleNameplateHealth = bool(clientOptions, "render_vehicle_nameplate_health", clientSettings.renderVehicleNameplateHealth, "Include health in vehicle nameplates.");
        clientSettings.renderOverlay = bool(clientOptions, "render_overlay", clientSettings.renderOverlay, "Render the vehicle information overlay.");
        clientSettings.renderAdvOverlay = bool(clientOptions, "render_advanced_overlay", clientSettings.renderAdvOverlay, "Render advanced vehicle information.");
        clientSettings.enableMouseAim = bool(clientOptions, "enable_mouse_aim", clientSettings.enableMouseAim, "Enable mouse aiming while riding combat vehicles.");

        vehicleStats.ammoBallistaBoltDamage = stat("ammo_ballista_bolt_damage", vehicleStats.ammoBallistaBoltDamage, 6, 80);
        vehicleStats.ammoBallistaBoltIronDamage = stat("ammo_ballista_bolt_iron_damage", vehicleStats.ammoBallistaBoltIronDamage, 6, 80);
        vehicleStats.ammoBallistaBoltFlameDamage = stat("ammo_ballista_bolt_flame_damage", vehicleStats.ammoBallistaBoltFlameDamage, 6, 80);
        vehicleStats.ammoBallistaBoltExplosiveDamage = stat("ammo_ballista_bolt_explosive_damage", vehicleStats.ammoBallistaBoltExplosiveDamage, 6, 80);
        vehicleStats.ammoCannonBall5kgDamage = stat("ammo_cannon_ball_5kg_damage", vehicleStats.ammoCannonBall5kgDamage, 6, 80);
        vehicleStats.ammoCannonBall10kgDamage = stat("ammo_cannon_ball_10kg_damage", vehicleStats.ammoCannonBall10kgDamage, 6, 80);
        vehicleStats.ammoCannonBall15kgDamage = stat("ammo_cannon_ball_15kg_damage", vehicleStats.ammoCannonBall15kgDamage, 6, 80);
        vehicleStats.ammoCannonBall25kgDamage = stat("ammo_cannon_ball_25kg_damage", vehicleStats.ammoCannonBall25kgDamage, 6, 80);
        vehicleStats.ammoCanisterDamage = stat("ammo_canister_damage", vehicleStats.ammoCanisterDamage, 6, 80);
        vehicleStats.ammoHwachaRocketDamage = stat("ammo_hwacha_rocket_damage", vehicleStats.ammoHwachaRocketDamage, 1, 80);
        vehicleStats.ammoHwachaRocketFlameDamage = stat("ammo_hwacha_rocket_flame_damage", vehicleStats.ammoHwachaRocketFlameDamage, 1, 80);
        vehicleStats.ammoHwachaRocketExplosiveDamage = stat("ammo_hwacha_rocket_explosive_damage", vehicleStats.ammoHwachaRocketExplosiveDamage, 1, 80);
        vehicleStats.vehicleBallistaHealth = health("vehicle_ballista_health", vehicleStats.vehicleBallistaHealth);
        vehicleStats.vehicleBallistaBoatHealth = health("vehicle_ballista_boat_health", vehicleStats.vehicleBallistaBoatHealth);
        vehicleStats.vehicleBatteringRamHealth = health("vehicle_battering_ram_health", vehicleStats.vehicleBatteringRamHealth);
        vehicleStats.vehicleCatapultHealth = health("vehicle_catapult_health", vehicleStats.vehicleCatapultHealth);
        vehicleStats.vehicleCatapultBoatHealth = health("vehicle_catapult_boat_health", vehicleStats.vehicleCatapultBoatHealth);
        vehicleStats.vehicleCannonHealth = health("vehicle_cannon_health", vehicleStats.vehicleCannonHealth);
        vehicleStats.vehicleChestCartHealth = health("vehicle_chest_cart_health", vehicleStats.vehicleChestCartHealth);
        vehicleStats.vehicleChestBoatHealth = health("vehicle_chest_boat_health", vehicleStats.vehicleChestBoatHealth);
        vehicleStats.vehicleHwachaHealth = health("vehicle_hwacha_health", vehicleStats.vehicleHwachaHealth);
        vehicleStats.vehicleTrebuchetHealth = health("vehicle_trebuchet_health", vehicleStats.vehicleTrebuchetHealth);
        vehicleStats.vehicleGiantTrebuchetHealth = health("vehicle_giant_trebuchet_health", vehicleStats.vehicleGiantTrebuchetHealth);
    }

    private boolean bool(String category, String key, boolean defaultValue, String comment) {
        return config.get(category, key, defaultValue, comment).getBoolean();
    }

    private int integer(String category, String key, int defaultValue, int min, int max, String comment) {
        return Math.max(min, Math.min(max, config.get(category, key, defaultValue, comment).getInt()));
    }

    private int stat(String key, int defaultValue, int min, int max) {
        return integer(tweakOptions, key, defaultValue, min, max, "Vehicle ammunition damage value.");
    }

    private int health(String key, int defaultValue) {
        return integer(tweakOptions, key, defaultValue, 50, 500, "Vehicle maximum health value.");
    }

    public void setMouseAimEnabled(boolean enabled) {
        clientSettings.enableMouseAim = enabled;
        config.get(clientOptions, "enable_mouse_aim", enabled, "Enable mouse aiming while riding combat vehicles.")
                .set(Boolean.toString(enabled));
        save();
    }

}
