package net.shadowmage.ancientwarfare.core.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.config.AWCoreStatics;
import net.shadowmage.ancientwarfare.core.gui.options.OptionsGuiFactory;
import net.shadowmage.ancientwarfare.core.input.InputHandler;
import net.shadowmage.ancientwarfare.core.init.AWCoreItems;
import net.shadowmage.ancientwarfare.core.manual.ManualContentRegistry;
import net.shadowmage.ancientwarfare.core.network.ClientNetworkScreens;
import net.shadowmage.ancientwarfare.core.registry.RegistryLoader;

/**
 * Forge 1.20.1 client proxy.
 */
@OnlyIn(Dist.CLIENT)
public class ClientProxy extends ClientProxyBase {
    public ClientProxy() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClientNetworkScreens.register();
            ItemProperties.register(AWCoreItems.LEGACY_COMPONENT.get(),
                    new ResourceLocation(AncientWarfareCore.MOD_ID, "legacy_component"),
                    (stack, level, entity, seed) -> stack.getDamageValue());
        });
    }

    public static Font getUnicodeFontRenderer() {
        return Minecraft.getInstance().font;
    }

    @Override
    public void preInit() {
        super.preInit();
        OptionsGuiFactory.register();
        MinecraftForge.EVENT_BUS.register(this);
        if (AWCoreStatics.DEBUG) {
            AncientWarfareCore.LOG.info("Debug resolution override is not applied on 1.20.1; window sizing is controlled by the client options.");
        }
    }

    @Override
    public void init() {
        InputHandler.initKeyBindings();
        RegistryLoader.registerParser(new ManualContentRegistry.ManualContentParser());
    }
}
