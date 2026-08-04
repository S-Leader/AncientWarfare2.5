package net.shadowmage.ancientwarfare.vehicle.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.input.InputHandler;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.RayTraceUtils;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.network.PacketVehicleInput;
import org.lwjgl.glfw.GLFW;

/**
 * Vehicle input bridge.
 *
 * <p>Movement, vertical movement and firing use Minecraft's own configurable
 * controls (forward/back/left/right, jump/sneak and attack).  Only actions that
 * do not have a vanilla equivalent register additional KeyMappings.</p>
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = AncientWarfareVehicles.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.MOD)
public final class VehicleInputHandler {
    private static final String CATEGORY = "keybind.category.awVehicles";

    private static final KeyMapping AMMO_PREV = new KeyMapping(
            AWVehicleStatics.KEY_VEHICLE_AMMO_PREV, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_T), CATEGORY);
    private static final KeyMapping AMMO_NEXT = new KeyMapping(
            AWVehicleStatics.KEY_VEHICLE_AMMO_NEXT, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_G), CATEGORY);
    private static final KeyMapping TURRET_LEFT = new KeyMapping(
            AWVehicleStatics.KEY_VEHICLE_TURRET_LEFT, VehicleKeyConflictContext.INSTANCE,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_Z), CATEGORY);
    private static final KeyMapping TURRET_RIGHT = new KeyMapping(
            AWVehicleStatics.KEY_VEHICLE_TURRET_RIGHT, VehicleKeyConflictContext.INSTANCE,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_X), CATEGORY);
    private static final KeyMapping MOUSE_AIM = new KeyMapping(
            AWVehicleStatics.KEY_VEHICLE_MOUSE_AIM, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_C), CATEGORY);
    private static final KeyMapping AMMO_SELECT = new KeyMapping(
            AWVehicleStatics.KEY_VEHICLE_AMMO_SELECT, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_V), CATEGORY);

    private static byte lastForwardInput;
    private static byte lastTurnInput;
    private static byte lastPowerInput;
    private static byte lastRotationInput;
    private static boolean attackWasDown;
    private static int lastVehicleId = Integer.MIN_VALUE;

    static {
        MinecraftForge.EVENT_BUS.register(new VehicleInputHandler());
    }

    private VehicleInputHandler() {
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(AMMO_PREV);
        event.register(AMMO_NEXT);
        event.register(TURRET_LEFT);
        event.register(TURRET_RIGHT);
        event.register(MOUSE_AIM);
        event.register(AMMO_SELECT);
    }

    /** Called by the vehicle client proxy after common input callbacks exist. */
    public static void initKeyBindings() {
        InputHandler.registerCallBack(MOUSE_AIM,
                () -> AncientWarfareVehicles.statics.setMouseAimEnabled(
                        !AWVehicleStatics.clientSettings.enableMouseAim));
        InputHandler.registerCallBack(TURRET_LEFT,
                new VehicleCallback(v -> v.firingHelper.handleAimKeyInput(0, -1)));
        InputHandler.registerCallBack(TURRET_RIGHT,
                new VehicleCallback(v -> v.firingHelper.handleAimKeyInput(0, 1)));
        InputHandler.registerCallBack(AMMO_NEXT,
                new VehicleCallback(v -> v.ammoHelper.setNextAmmo()));
        InputHandler.registerCallBack(AMMO_PREV,
                new VehicleCallback(v -> v.ammoHelper.setPreviousAmmo()));
        InputHandler.registerCallBack(AMMO_SELECT,
                new VehicleCallback(VehicleInputHandler::handleAmmoSelectAction));
    }

    private static void handleAmmoSelectAction(VehicleBase vehicle) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        if (!vehicle.isAmmoLoaded()) {
            minecraft.player.displayClientMessage(
                    Component.translatable("gui.ancientwarfarevehicles.ammo.no_ammo"), true);
            return;
        }
        if (!vehicle.vehicleType.getValidAmmoTypes().isEmpty()) {
            AWMenuTypes.open(minecraft.player, NetworkHandler.GUI_VEHICLE_AMMO_SELECTION, vehicle.getId());
        }
    }

    private static void handleFireAction(VehicleBase vehicle) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        String configName = vehicle.vehicleType.getConfigName();
        if (!vehicle.isAmmoLoaded()
                && !(configName.equals("battering_ram")
                || configName.equals("boat_transport")
                || configName.equals("chest_cart"))) {
            minecraft.player.displayClientMessage(
                    Component.translatable("gui.ancientwarfarevehicles.ammo.no_ammo"), true);
        }
        if (vehicle.isAimable()) {
            vehicle.firingHelper.handleFireInput();
        }
    }

    private static final float MAX_RANGE = 140;

    private static HitResult getPlayerLookTargetClient(Player player, Entity excludedEntity) {
        return RayTraceUtils.getPlayerTarget(player, MAX_RANGE, 1.0F);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || !(minecraft.player.getVehicle() instanceof VehicleBase vehicle)) {
            resetInputState();
            return;
        }

        handleVanillaAttackKey(minecraft, vehicle);
        handleMovementInput(minecraft, vehicle);
        if (AWVehicleStatics.clientSettings.enableMouseAim) {
            handleMouseAimUpdate(vehicle);
        }
    }

    private static void handleVanillaAttackKey(Minecraft minecraft, VehicleBase vehicle) {
        boolean attackDown = minecraft.options.keyAttack.isDown();
        // Follow vanilla attack-key semantics: a held attack key may fire again
        // as soon as the vehicle has completed its reload/cooldown.  Invalid/no
        // ammo feedback is still emitted only on the initial press.
        if (attackDown && (!attackWasDown || vehicle.firingHelper.isReadyToFire())) {
            handleFireAction(vehicle);
        }
        attackWasDown = attackDown;
    }

    private static void handleMovementInput(Minecraft minecraft, VehicleBase vehicle) {
        byte forward = axis(minecraft.options.keyUp, minecraft.options.keyDown);
        byte turn = axis(minecraft.options.keyRight, minecraft.options.keyLeft);
        byte power = axis(minecraft.options.keyJump, minecraft.options.keyShift);
        byte rotation = axis(TURRET_RIGHT, TURRET_LEFT);

        // The old R/F controls both moved vertically and adjusted turret pitch.
        // Preserve that behaviour on the player's vanilla jump/sneak bindings.
        if (power != 0 && vehicle.tickCount % 2 == 0) {
            vehicle.firingHelper.handleAimKeyInput(-power, 0);
        }

        boolean vehicleChanged = lastVehicleId != vehicle.getId();
        boolean inputChanged = forward != lastForwardInput
                || turn != lastTurnInput
                || power != lastPowerInput
                || rotation != lastRotationInput;

        if (vehicleChanged || inputChanged || vehicle.tickCount % 20 == 0) {
            PacketVehicleInput packet = new PacketVehicleInput(vehicle);
            packet.setForwardInput(forward);
            packet.setTurnInput(turn);
            packet.setPowerInput(power);
            packet.setRotationInput(rotation);
            NetworkHandler.sendToServer(packet);

            lastVehicleId = vehicle.getId();
            lastForwardInput = forward;
            lastTurnInput = turn;
            lastPowerInput = power;
            lastRotationInput = rotation;
        }
    }

    private static byte axis(KeyMapping positive, KeyMapping negative) {
        if (positive.isDown() == negative.isDown()) {
            return 0;
        }
        return (byte) (positive.isDown() ? 1 : -1);
    }

    private static void handleMouseAimUpdate(VehicleBase vehicle) {
        if (vehicle.tickCount % 5 == 0) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player != null) {
                HitResult target = getPlayerLookTargetClient(minecraft.player, vehicle);
                if (target != null) {
                    vehicle.firingHelper.handleAimInput(target.getLocation());
                }
            }
        }
    }

    private static void resetInputState() {
        lastVehicleId = Integer.MIN_VALUE;
        lastForwardInput = 0;
        lastTurnInput = 0;
        lastPowerInput = 0;
        lastRotationInput = 0;
        attackWasDown = false;
    }
}
