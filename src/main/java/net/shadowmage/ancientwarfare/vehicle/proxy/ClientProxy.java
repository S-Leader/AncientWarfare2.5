package net.shadowmage.ancientwarfare.vehicle.proxy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelRegistryHelper;
import net.shadowmage.ancientwarfare.vehicle.AncientWarfareVehicles;
import net.shadowmage.ancientwarfare.vehicle.gui.GuiVehicleAmmoSelection;
import net.shadowmage.ancientwarfare.vehicle.gui.GuiVehicleInventory;
import net.shadowmage.ancientwarfare.vehicle.gui.GuiVehicleStats;
import net.shadowmage.ancientwarfare.vehicle.input.VehicleInputHandler;
import net.shadowmage.ancientwarfare.vehicle.init.AWVehicleEntities;
import net.shadowmage.ancientwarfare.vehicle.item.ItemBaseVehicle;
import net.shadowmage.ancientwarfare.vehicle.render.RenderMissile;
import net.shadowmage.ancientwarfare.vehicle.render.RenderOverlay;
import net.shadowmage.ancientwarfare.vehicle.render.RenderOverlayAdvanced;
import net.shadowmage.ancientwarfare.vehicle.render.RenderVehicle;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerRenderers);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerModels);
    }

    private void registerModels(ModelEvent.RegisterAdditional event) {
        ForgeRegistries.ITEMS.getValues().stream()
                .filter(ItemBaseVehicle.class::isInstance)
                .map(ItemBaseVehicle.class::cast)
                .forEach(ItemBaseVehicle::registerClient);
        LegacyModelRegistryHelper.registerAdditional(event);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(AWVehicleEntities.MISSILE.get(), RenderMissile::new);
        event.registerEntityRenderer(AWVehicleEntities.VEHICLE.get(), RenderVehicle::new);
    }

    @Override
    public void preInit() {
        MinecraftForge.EVENT_BUS.register(new RenderOverlay());
        MinecraftForge.EVENT_BUS.register(new RenderOverlayAdvanced());
    }

    @Override
    public void init() {
        VehicleInputHandler.initKeyBindings();
    }
}
