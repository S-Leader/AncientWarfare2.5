package net.shadowmage.ancientwarfare.npc.event;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.init.AWNPCItems;
import net.shadowmage.ancientwarfare.npc.item.ItemNpcSpawner;
import net.shadowmage.ancientwarfare.npc.item.ItemShield;

@Mod.EventBusSubscriber(modid = AncientWarfareNPC.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    private static final ResourceLocation BLOCKING = new ResourceLocation(AncientWarfareNPC.MOD_ID, "blocking");
    private static final ResourceLocation NPC_VARIANT = new ResourceLocation(AncientWarfareNPC.MOD_ID, "npc_variant");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ForgeRegistries.ITEMS.getValues().stream()
                    .filter(ItemShield.class::isInstance)
                    .filter(item -> {
                        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
                        return key != null && AncientWarfareNPC.MOD_ID.equals(key.getNamespace());
                    })
                    .forEach(ClientEvents::registerShieldProperties);

            ItemProperties.register(AWNPCItems.NPC_SPAWNER.get(), NPC_VARIANT,
                    (stack, level, entity, seed) -> ItemNpcSpawner.getModelVariantProperty(stack));
        });
    }

    private static void registerShieldProperties(Item item) {
        ItemProperties.register(item, BLOCKING,
                (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
    }

    private ClientEvents() {
    }
}
