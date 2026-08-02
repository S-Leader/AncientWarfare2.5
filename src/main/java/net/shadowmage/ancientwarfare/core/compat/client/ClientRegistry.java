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
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.tile.LegacyBlockEntityRegistry;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Central bridge for legacy client registrations.
 */
@Mod.EventBusSubscriber(modid = AncientWarfareCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientRegistry {
    private static final Set<KeyMapping> KEY_MAPPINGS = new LinkedHashSet<>();

    private ClientRegistry() {
    }

    public static void registerKeyBinding(KeyMapping mapping) {
        KEY_MAPPINGS.add(mapping);
    }

    /**
     * Block entity renderer registration needs registered BlockEntityType
     * instances, not legacy implementation classes. Calls are retained here
     * while those types are migrated by the central registry pass.
     */
    public static void bindTileEntitySpecialRenderer(Class<?> tileClass, Object renderer) {
        // Registered from EntityRenderersEvent after block entity types exist.
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends BlockEntity> void registerBlockEntityRenderer(
            EntityRenderersEvent.RegisterRenderers event, Class<T> tileClass,
            BlockEntityRendererProvider<? super T> provider) {
        for (BlockEntityType<?> type : LegacyBlockEntityRegistry.getTypesForClass(tileClass)) {
            event.registerBlockEntityRenderer((BlockEntityType) type, (BlockEntityRendererProvider) provider);
        }
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        KEY_MAPPINGS.forEach(event::register);
    }
}
