package net.shadowmage.ancientwarfare.core.compat.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

import java.util.LinkedHashSet;
import java.util.Set;

/** Client-only registrations that are not themselves Forge registries. */
@Mod.EventBusSubscriber(modid = AncientWarfareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientRegistry {
    private static final Set<KeyMapping> KEY_MAPPINGS = new LinkedHashSet<>();

    private ClientRegistry() {
    }

    public static void registerKeyBinding(KeyMapping mapping) {
        KEY_MAPPINGS.add(mapping);
    }

    /**
     * Registers a renderer against the actual DeferredRegister handle.  There is deliberately
     * no class-to-type lookup table: the registry object is the single source of identity.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends BlockEntity> void registerBlockEntityRenderer(
            EntityRenderersEvent.RegisterRenderers event,
            RegistryObject<? extends BlockEntityType<? extends T>> type,
            BlockEntityRendererProvider<? super T> provider) {
        event.registerBlockEntityRenderer((BlockEntityType) type.get(), (BlockEntityRendererProvider) provider);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        KEY_MAPPINGS.forEach(event::register);
    }
}
