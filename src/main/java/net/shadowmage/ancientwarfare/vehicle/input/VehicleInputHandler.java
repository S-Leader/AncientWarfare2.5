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
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.core.compat.client.ClientRegistry;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.input.InputHandler;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.RayTraceUtils;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.network.PacketVehicleInput;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class VehicleInputHandler {
    private static final String CATEGORY = "keybind.category.awVehicles";
    private static final KeyMapping FORWARD = new KeyMapping(AWVehicleStatics.KEY_VEHICLE_FORWARD, VehicleKeyConflictContext.INSTANCE, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_W),
            CATEGORY);
    private static final KeyMapping REVERSE = new KeyMapping(AWVehicleStatics.KEY_VEHICLE_REVERSE, VehicleKeyConflictContext.INSTANCE, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_S),
            CATEGORY);
    private static final KeyMapping LEFT = new KeyMapping(AWVehicleStatics.KEY_VEHICLE_LEFT, VehicleKeyConflictContext.INSTANCE, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_A), CATEGORY);
    private static final KeyMapping RIGHT = new KeyMapping(AWVehicleStatics.KEY_VEHICLE_RIGHT, VehicleKeyConflictContext.INSTANCE, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_D), CATEGORY);
    private static final KeyMapping ASCEND_AIM_UP = new KeyMapping(AWVehicleStatics.KEY_VEHICLE_ASCEND_AIM_UP, VehicleKeyConflictContext.INSTANCE,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_R), CATEGORY);
    private static final KeyMapping DESCEND_AIM_DOWN = new KeyMapping(AWVehicleStatics.KEY_VEHICLE_DESCEND_AIM_DOWN, VehicleKeyConflictContext.INSTANCE,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F), CATEGORY);
    private static final KeyMapping FIRE = new KeyMapping(AWVehicleStatics.KEY_VEHICLE_FIRE, VehicleKeyConflictContext.INSTANCE, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_SPACE), CATEGORY);
    private static final KeyMapping AMMO_PREV = new KeyMapping(AWVehicleStatics.KEY_VEHICLE_AMMO_PREV, VehicleKeyConflictContext.INSTANCE, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_T),
            CATEGORY);
    private static final KeyMapping AMMO_NEXT = new KeyMapping(AWVehicleStatics.KEY_VEHICLE_AMMO_NEXT, VehicleKeyConflictContext.INSTANCE, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_G),
            CATEGORY);
    private static final KeyMapping TURRET_LEFT = new KeyMapping(AWVehicleStatics.KEY_VEHICLE_TURRET_LEFT, VehicleKeyConflictContext.INSTANCE, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_Z),
            CATEGORY);
    private static final KeyMapping TURRET_RIGHT = new KeyMapping(AWVehicleStatics.KEY_VEHICLE_TURRET_RIGHT, VehicleKeyConflictContext.INSTANCE, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_X),
            CATEGORY);
    private static final KeyMapping MOUSE_AIM = new KeyMapping(AWVehicleStatics.KEY_VEHICLE_MOUSE_AIM, VehicleKeyConflictContext.INSTANCE, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_C),
            CATEGORY);
    private static final KeyMapping AMMO_SELECT = new KeyMapping(AWVehicleStatics.KEY_VEHICLE_AMMO_SELECT, VehicleKeyConflictContext.INSTANCE, InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_V),
            CATEGORY);

    private static final Set<Integer> releaseableKeys = new HashSet<>();
    private static boolean trackedKeyReleased = false;
    private static final Set<IVehicleMovementHandler> vehicleMovementHandlers = new HashSet<>();

    static {
        MinecraftForge.EVENT_BUS.register(new VehicleInputHandler());
    }

    private VehicleInputHandler() {
    }

    public static void initKeyBindings() {
        ClientRegistry.registerKeyBinding(FORWARD);
        ClientRegistry.registerKeyBinding(REVERSE);
        ClientRegistry.registerKeyBinding(LEFT);
        ClientRegistry.registerKeyBinding(RIGHT);
        ClientRegistry.registerKeyBinding(ASCEND_AIM_UP);
        ClientRegistry.registerKeyBinding(DESCEND_AIM_DOWN);
        ClientRegistry.registerKeyBinding(FIRE);
        ClientRegistry.registerKeyBinding(AMMO_PREV);
        ClientRegistry.registerKeyBinding(AMMO_NEXT);
        ClientRegistry.registerKeyBinding(TURRET_LEFT);
        ClientRegistry.registerKeyBinding(TURRET_RIGHT);
        ClientRegistry.registerKeyBinding(MOUSE_AIM);
        ClientRegistry.registerKeyBinding(AMMO_SELECT);

        initCallbacks();
        initReleaseableKeys();
    }

    private static void initReleaseableKeys() {
        vehicleMovementHandlers.add(new IVehicleMovementHandler.Impl(FORWARD, REVERSE, p -> p.setForwardInput((byte) 1)));
        vehicleMovementHandlers.add(new IVehicleMovementHandler.Impl(REVERSE, FORWARD, p -> p.setForwardInput((byte) -1)));
        vehicleMovementHandlers.add(new IVehicleMovementHandler.Impl(RIGHT, LEFT, p -> p.setTurnInput((byte) 1)));
        vehicleMovementHandlers.add(new IVehicleMovementHandler.Impl(LEFT, RIGHT, p -> p.setTurnInput((byte) -1)));
        vehicleMovementHandlers.add(new IVehicleMovementHandler.Impl(ASCEND_AIM_UP, DESCEND_AIM_DOWN, p -> p.setPowerInput((byte) 1)));
        vehicleMovementHandlers.add(new IVehicleMovementHandler.Impl(DESCEND_AIM_DOWN, ASCEND_AIM_UP, p -> p.setPowerInput((byte) -1)));
        vehicleMovementHandlers.add(new IVehicleMovementHandler.Impl(TURRET_RIGHT, TURRET_LEFT, p -> p.setRotationInput((byte) 1)));
        vehicleMovementHandlers.add(new IVehicleMovementHandler.Impl(TURRET_LEFT, TURRET_RIGHT, p -> p.setRotationInput((byte) -1)));

        releaseableKeys.add(FORWARD.getKey().getValue());
        releaseableKeys.add(REVERSE.getKey().getValue());
        releaseableKeys.add(LEFT.getKey().getValue());
        releaseableKeys.add(RIGHT.getKey().getValue());
        releaseableKeys.add(ASCEND_AIM_UP.getKey().getValue());
        releaseableKeys.add(DESCEND_AIM_DOWN.getKey().getValue());
        releaseableKeys.add(TURRET_LEFT.getKey().getValue());
        releaseableKeys.add(TURRET_RIGHT.getKey().getValue());
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key evt) {
        if (evt.getAction() == GLFW.GLFW_RELEASE && releaseableKeys.contains(evt.getKey())) {
            trackedKeyReleased = true;
        }
    }

    private static void initCallbacks() {
        InputHandler.registerCallBack(MOUSE_AIM,
                () -> AncientWarfareVehicles.statics.setMouseAimEnabled(!AWVehicleStatics.clientSettings.enableMouseAim));
        InputHandler.registerCallBack(FIRE, new VehicleCallback(VehicleInputHandler::handleFireAction));
        InputHandler.registerCallBack(ASCEND_AIM_UP, new VehicleCallback(v -> v.firingHelper.handleAimKeyInput(-1, 0)));
        InputHandler.registerCallBack(DESCEND_AIM_DOWN, new VehicleCallback(v -> v.firingHelper.handleAimKeyInput(1, 0)));
        InputHandler.registerCallBack(TURRET_LEFT, new VehicleCallback(v -> v.firingHelper.handleAimKeyInput(0, -1)));
        InputHandler.registerCallBack(TURRET_RIGHT, new VehicleCallback(v -> v.firingHelper.handleAimKeyInput(0, 1)));
        InputHandler.registerCallBack(AMMO_NEXT, new VehicleCallback(v -> v.ammoHelper.setNextAmmo()));
        InputHandler.registerCallBack(AMMO_PREV, new VehicleCallback(v -> v.ammoHelper.setPreviousAmmo()));
        InputHandler.registerCallBack(AMMO_SELECT, new VehicleCallback(VehicleInputHandler::handleAmmoSelectAction));
    }

    private static void handleAmmoSelectAction(VehicleBase vehicle) {
        if (!vehicle.isAmmoLoaded()) {
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("gui.ancientwarfarevehicles.ammo.no_ammo"), true);
            return;
        }

        if (!vehicle.vehicleType.getValidAmmoTypes().isEmpty()) {
            AWMenuTypes.open(Minecraft.getInstance().player, NetworkHandler.GUI_VEHICLE_AMMO_SELECTION, vehicle.getId());
        }
    }

    private static void handleFireAction(VehicleBase vehicle) {
        String configName = vehicle.vehicleType.getConfigName();
        if (!vehicle.isAmmoLoaded() && !(configName.equals("battering_ram") || configName.equals("boat_transport") || configName.equals("chest_cart"))) {
            Minecraft.getInstance().player.displayClientMessage(Component.translatable("gui.ancientwarfarevehicles.ammo.no_ammo"), true);
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
    public void onTickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getVehicle() instanceof VehicleBase) {
            VehicleBase vehicle = (VehicleBase) mc.player.getVehicle();
            handleTickInput(vehicle);
            if (AWVehicleStatics.clientSettings.enableMouseAim) {
                handleMouseAimUpdate(vehicle);
            }
        }
    }

    private void handleMouseAimUpdate(VehicleBase vehicle) {
        if (vehicle.tickCount % 5 == 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        HitResult pos = getPlayerLookTargetClient(mc.player, vehicle);
        if (pos != null) {
            vehicle.firingHelper.handleAimInput(pos.getLocation());
        }
    }

    private static void handleTickInput(VehicleBase vehicle) {
        if (trackedKeyReleased || vehicle.tickCount % 20 == 0) {
            trackedKeyReleased = false;

            PacketVehicleInput pkt = new PacketVehicleInput(vehicle);

            vehicleMovementHandlers.stream().filter(h -> h.getKeyBinding().isDown() && !h.getReverseKeyBinding().isDown())
                    .forEach(h -> h.updatePacket(pkt));

            NetworkHandler.sendToServer(pkt);
        }
    }

}
