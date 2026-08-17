package net.shadowmage.ancientwarfare.core.init;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketGui;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Forge 1.20.1 native menu registry.
 *
 * Every AW menu is identified by a stable ResourceLocation, e.g.
 * ancientwarfare:npc_inventory. No numeric/legacy GUI id participates in
 * registration, opening, screen lookup or network requests.
 */
public final class AWMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, AncientWarfareCore.MOD_ID);

    private static final Map<ResourceLocation, MenuRegistration> MENUS_BY_ID = new LinkedHashMap<>();

    /** Used only by local child screens that need a harmless menu instance. */
    public static final RegistryObject<MenuType<ContainerBase>> CLIENT_ONLY = MENUS.register(
            "client_only",
            () -> new MenuType<>((containerId, inventory) -> {
                throw new UnsupportedOperationException("client_only is not a network-openable menu");
            }, FeatureFlags.DEFAULT_FLAGS)
    );

    private AWMenuTypes() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }

    public static synchronized void registerMenu(ResourceLocation id, Class<? extends ContainerBase> containerClass) {
        if (!AncientWarfareCore.MOD_ID.equals(id.getNamespace())) {
            throw new IllegalArgumentException("Ancient Warfare menu id must use namespace '"
                    + AncientWarfareCore.MOD_ID + "': " + id);
        }

        MenuRegistration existing = MENUS_BY_ID.get(id);
        if (existing != null) {
            if (existing.containerClass() != containerClass) {
                throw new IllegalStateException("Menu " + id + " already belongs to "
                        + existing.containerClass().getName());
            }
            return;
        }

        final Constructor<? extends ContainerBase> constructor;
        try {
            constructor = containerClass.getConstructor(Player.class, int.class, int.class, int.class);
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException(containerClass.getName()
                    + " must expose (Player,int,int,int) for native menu registration", exception);
        }

        RegistryObject<MenuType<ContainerBase>> menuType = MENUS.register(id.getPath(), () ->
                IForgeMenuType.create((windowId, inventory, data) ->
                        createClientMenu(id, windowId, inventory, data)));

        MENUS_BY_ID.put(id, new MenuRegistration(id, containerClass, constructor, menuType));
    }

    public static Collection<MenuRegistration> registrations() {
        return Collections.unmodifiableCollection(MENUS_BY_ID.values());
    }

    @Nullable
    public static MenuRegistration getRegistration(ResourceLocation id) {
        return MENUS_BY_ID.get(id);
    }

    @Nullable
    public static MenuType<ContainerBase> getMenuType(ResourceLocation id) {
        MenuRegistration registration = MENUS_BY_ID.get(id);
        return registration == null ? null : registration.menuType().get();
    }

    public static void open(Player player, ResourceLocation id) {
        open(player, id, 0, 0, 0);
    }

    public static void open(Player player, ResourceLocation id, BlockPos pos) {
        open(player, id, pos.getX(), pos.getY(), pos.getZ());
    }

    public static void open(Player player, ResourceLocation id, int firstValue) {
        open(player, id, firstValue, 0, 0);
    }

    public static void open(Player player, ResourceLocation id, int x, int y, int z) {
        if (player.level().isClientSide()) {
            // World/item interactions also execute server-side. A request packet is
            // only needed for screen-to-screen switches and the client vehicle key.
            boolean replacingMenu = player.containerMenu instanceof ContainerBase;
            boolean clientOnlyVehicleAction = NetworkHandler.GUI_VEHICLE_AMMO_SELECTION.equals(id);
            if (replacingMenu || clientOnlyVehicleAction) {
                PacketGui packet = new PacketGui();
                packet.setMenuRequest(id, x, y, z);
                NetworkHandler.sendToServer(packet);
            }
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            openServer(serverPlayer, id, x, y, z);
        }
    }

    /** Handles menu switches initiated from an already-open client screen. */
    public static void openRequested(Player player, ResourceLocation id, int x, int y, int z) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        boolean replacingMenu = serverPlayer.containerMenu instanceof ContainerBase;
        boolean validVehicleRequest = NetworkHandler.GUI_VEHICLE_AMMO_SELECTION.equals(id)
                && serverPlayer.getVehicle() != null
                && serverPlayer.getVehicle().getId() == x;
        if (!replacingMenu && !validVehicleRequest) {
            return;
        }

        openServer(serverPlayer, id, x, y, z);
    }

    private static void openServer(ServerPlayer player, ResourceLocation id, int x, int y, int z) {
        MenuRegistration registration = MENUS_BY_ID.get(id);
        if (registration == null) {
            AncientWarfareCore.LOG.error("No registered MenuType for {}", id);
            return;
        }

        NetworkHooks.openScreen(
                player,
                new SimpleMenuProvider(
                        (windowId, inventory, menuPlayer) ->
                                createMenu(registration, windowId, menuPlayer, x, y, z),
                        Component.translatable("gui.ancientwarfare.title")
                ),
                buffer -> writeOpeningData(player, buffer, x, y, z)
        );

        if (player.containerMenu instanceof ContainerBase container) {
            container.sendInitData();
        }
    }

    private static ContainerBase createClientMenu(
            ResourceLocation id, int windowId, Inventory inventory, FriendlyByteBuf data) {
        int x = data.readInt();
        int y = data.readInt();
        int z = data.readInt();
        CompoundTag blockEntityData = data.readNbt();
        restoreClientBlockEntity(inventory.player, x, y, z, blockEntityData);

        MenuRegistration registration = MENUS_BY_ID.get(id);
        if (registration == null) {
            throw new IllegalStateException("Missing native MenuType registration for " + id);
        }
        return createMenu(registration, windowId, inventory.player, x, y, z);
    }

    private static ContainerBase createMenu(
            MenuRegistration registration, int windowId, Player player, int x, int y, int z) {
        return ContainerBase.createForMenu(
                registration.menuType().get(),
                windowId,
                () -> {
                    try {
                        return registration.constructor().newInstance(player, x, y, z);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                        Throwable cause = exception instanceof InvocationTargetException invocation
                                && invocation.getCause() != null ? invocation.getCause() : exception;
                        throw new MenuCreationException(registration.id(), cause);
                    }
                }
        );
    }

    private static void writeOpeningData(ServerPlayer player, FriendlyByteBuf buffer, int x, int y, int z) {
        buffer.writeInt(x);
        buffer.writeInt(y);
        buffer.writeInt(z);
        BlockEntity blockEntity = player.level().getBlockEntity(new BlockPos(x, y, z));
        buffer.writeNbt(blockEntity == null ? null : blockEntity.saveWithFullMetadata());
    }

    private static void restoreClientBlockEntity(
            Player player, int x, int y, int z, @Nullable CompoundTag data) {
        if (data == null || !player.level().isClientSide()) {
            return;
        }
        BlockPos pos = new BlockPos(x, y, z);
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (blockEntity == null) {
            BlockState state = player.level().getBlockState(pos);
            blockEntity = BlockEntity.loadStatic(pos, state, data);
            if (blockEntity != null) {
                player.level().setBlockEntity(blockEntity);
            }
        } else {
            blockEntity.load(data);
        }
    }

    public record MenuRegistration(
            ResourceLocation id,
            Class<? extends ContainerBase> containerClass,
            Constructor<? extends ContainerBase> constructor,
            RegistryObject<MenuType<ContainerBase>> menuType) {
    }

    private static final class MenuCreationException extends RuntimeException {
        private MenuCreationException(ResourceLocation id, Throwable cause) {
            super("Unable to create menu " + id, cause);
        }
    }
}
