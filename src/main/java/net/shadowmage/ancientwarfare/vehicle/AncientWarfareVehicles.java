package net.shadowmage.ancientwarfare.vehicle;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketBase;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.container.ContainerVehicle;
import net.shadowmage.ancientwarfare.vehicle.container.ContainerVehicleInventory;
import net.shadowmage.ancientwarfare.vehicle.init.AWVehicleEntities;
import net.shadowmage.ancientwarfare.vehicle.init.AWVehicleItems;
import net.shadowmage.ancientwarfare.vehicle.init.AWVehicleSounds;
import net.shadowmage.ancientwarfare.vehicle.network.*;
import net.shadowmage.ancientwarfare.vehicle.proxy.ClientProxy;
import net.shadowmage.ancientwarfare.vehicle.proxy.CommonProxy;
import net.shadowmage.ancientwarfare.vehicle.registry.AmmoRegistry;
import net.shadowmage.ancientwarfare.vehicle.registry.ArmorRegistry;
import net.shadowmage.ancientwarfare.vehicle.registry.SmeltingRecipeRegistry;
import net.shadowmage.ancientwarfare.vehicle.registry.UpgradeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Forge 1.20.1 entry point for the vehicle module.
 */
@Mod(AncientWarfareVehicles.MOD_ID)
public final class AncientWarfareVehicles {
    public static final String MOD_ID = "ancientwarfarevehicle";
    public static final RegistryObject<CreativeModeTab> TAB = AWVehicleTab.TAB;
    public static final Logger LOG = LogManager.getLogger(MOD_ID);

    public static AncientWarfareVehicles instance;
    public static CommonProxy proxy;
    public static AWVehicleStatics statics;

    public AncientWarfareVehicles() {
        instance = this;
        statics = new AWVehicleStatics("AncientWarfareVehicle");
        proxy = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        AWVehicleTab.register(modBus);
        // VehicleType definitions reference ammo/armor/upgrades while they are constructed.
        // Prepare and attach those registries before concrete vehicle item suppliers are declared.
        AmmoRegistry.register(modBus);
        ArmorRegistry.register(modBus);
        UpgradeRegistry.register(modBus);
        AWVehicleItems.register(modBus);
        AWVehicleEntities.register(modBus);
        SmeltingRecipeRegistry.register(modBus);
        AWVehicleSounds.register(modBus);
        modBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        registerPackets();
        NetworkHandler.registerContainer(NetworkHandler.GUI_VEHICLE_INVENTORY, ContainerVehicleInventory.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_VEHICLE_AMMO_SELECTION, ContainerVehicle.class);
        NetworkHandler.registerContainer(NetworkHandler.GUI_VEHICLE_STATS, ContainerVehicle.class);
        proxy.preInit();
    }

    private void registerPackets() {
        PacketBase.registerPacketType(NetworkHandler.PACKET_AIM_UPDATE, PacketAimUpdate.class, PacketAimUpdate::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_AMMO_SELECT, PacketAmmoSelect.class, PacketAmmoSelect::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_AMMO_UPDATE, PacketAmmoUpdate.class, PacketAmmoUpdate::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_FIRE_UPDATE, PacketFireUpdate.class, PacketFireUpdate::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_PACK_COMMAND, PacketPackCommand.class, PacketPackCommand::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_SINGLE_AMMO_UPDATE, PacketSingleAmmoUpdate.class, PacketSingleAmmoUpdate::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_UPGRADE_UPDATE, PacketUpgradeUpdate.class, PacketUpgradeUpdate::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_VEHICLE_INPUT, PacketVehicleInput.class, PacketVehicleInput::new);
        PacketBase.registerPacketType(NetworkHandler.PACKET_VEHICLE_MOVE, PacketVehicleMove.class, PacketVehicleMove::new);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            proxy.init();
            statics.save();
        });
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        statics.save();
    }
}
