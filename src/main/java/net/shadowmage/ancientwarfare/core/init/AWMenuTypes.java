package net.shadowmage.ancientwarfare.core.init;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
import java.util.*;

/**
 * Native Forge menu registrations for every Ancient Warfare GUI.
 * <p>
 * Existing legacy container constructors remain unchanged, but every GUI now
 * has its own MenuType and is opened through NetworkHooks.openScreen.
 */
public final class AWMenuTypes {
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, AncientWarfareCore.MOD_ID);

    private static final Map<Integer, MenuRegistration> MENUS_BY_LEGACY_ID = new LinkedHashMap<>();

    /**
     * Used only by local child screens that need a harmless menu instance.
     */
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

    /**
     * Called by the existing module registration sites during mod construction.
     */
    public static synchronized void registerLegacy(int legacyGuiId, Class<? extends ContainerBase> containerClass) {
        MenuRegistration existing = MENUS_BY_LEGACY_ID.get(legacyGuiId);
        if (existing != null) {
            if (existing.containerClass() != containerClass) {
                throw new IllegalStateException("GUI id " + legacyGuiId + " already belongs to "
                        + existing.containerClass().getName());
            }
            return;
        }

        final Constructor<? extends ContainerBase> constructor;
        try {
            constructor = containerClass.getConstructor(Player.class, int.class, int.class, int.class);
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException(containerClass.getName()
                    + " must expose (Player,int,int,int) for menu registration", exception);
        }

        String registryName = registryName(containerClass, legacyGuiId);
        RegistryObject<MenuType<ContainerBase>> menuType = MENUS.register(registryName, () ->
                IForgeMenuType.create((windowId, inventory, data) ->
                        createClientMenu(legacyGuiId, windowId, inventory, data)));

        MENUS_BY_LEGACY_ID.put(legacyGuiId,
                new MenuRegistration(legacyGuiId, containerClass, constructor, menuType));
    }

    public static Collection<MenuRegistration> registrations() {
        return Collections.unmodifiableCollection(MENUS_BY_LEGACY_ID.values());
    }

    @Nullable
    public static MenuRegistration getRegistration(int legacyGuiId) {
        return MENUS_BY_LEGACY_ID.get(legacyGuiId);
    }

    public static void open(Player player, int legacyGuiId) {
        open(player, legacyGuiId, 0, 0, 0);
    }

    public static void open(Player player, int legacyGuiId, BlockPos pos) {
        open(player, legacyGuiId, pos.getX(), pos.getY(), pos.getZ());
    }

    public static void open(Player player, int legacyGuiId, int firstValue) {
        open(player, legacyGuiId, firstValue, 0, 0);
    }

    public static void open(Player player, int legacyGuiId, int x, int y, int z) {
        if (player.level().isClientSide()) {
            // Normal world/item interactions also run on the server. Only menu
            // switches and the client-only vehicle key need a request packet.
            boolean replacingMenu = player.containerMenu instanceof ContainerBase;
            boolean clientOnlyVehicleAction = legacyGuiId == NetworkHandler.GUI_VEHICLE_AMMO_SELECTION;
            if (replacingMenu || clientOnlyVehicleAction) {
                PacketGui packet = new PacketGui();
                packet.setMenuRequest(legacyGuiId, x, y, z);
                NetworkHandler.sendToServer(packet);
            }
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            openServer(serverPlayer, legacyGuiId, x, y, z);
        }
    }

    /**
     * Handles GUI switches initiated from a client screen or client-only key.
     */
    public static void openRequested(Player player, int legacyGuiId, int x, int y, int z) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        boolean replacingMenu = serverPlayer.containerMenu instanceof ContainerBase;
        boolean validVehicleRequest = legacyGuiId == NetworkHandler.GUI_VEHICLE_AMMO_SELECTION
                && serverPlayer.getVehicle() != null
                && serverPlayer.getVehicle().getId() == x;
        if (!replacingMenu && !validVehicleRequest) {
            return;
        }

        openServer(serverPlayer, legacyGuiId, x, y, z);
    }

    private static void openServer(ServerPlayer player, int legacyGuiId, int x, int y, int z) {
        MenuRegistration registration = MENUS_BY_LEGACY_ID.get(legacyGuiId);
        if (registration == null) {
            AncientWarfareCore.LOG.error("No registered MenuType for GUI id {}", legacyGuiId);
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
            int legacyGuiId, int windowId, Inventory inventory, FriendlyByteBuf data) {
        int x = data.readInt();
        int y = data.readInt();
        int z = data.readInt();
        CompoundTag blockEntityData = data.readNbt();
        restoreClientBlockEntity(inventory.player, x, y, z, blockEntityData);

        MenuRegistration registration = MENUS_BY_LEGACY_ID.get(legacyGuiId);
        if (registration == null) {
            throw new IllegalStateException("Missing client MenuType registration for GUI id " + legacyGuiId);
        }
        return createMenu(registration, windowId, inventory.player, x, y, z);
    }

    private static ContainerBase createMenu(
            MenuRegistration registration, int windowId, Player player, int x, int y, int z) {
        return ContainerBase.createForMenu(
                registration.menuType().get(),
                windowId,
                registration.legacyGuiId(),
                () -> {
                    try {
                        return registration.constructor().newInstance(player, x, y, z);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                        Throwable cause = exception instanceof InvocationTargetException invocation
                                && invocation.getCause() != null ? invocation.getCause() : exception;
                        throw new MenuCreationException(registration.legacyGuiId(), cause);
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

    private static String registryName(Class<?> containerClass, int legacyGuiId) {
        String simpleName = containerClass.getSimpleName().replaceFirst("^Container", "");
        String snakeCase = simpleName
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toLowerCase(Locale.ROOT);
        return snakeCase + "_" + legacyGuiId;
    }

    public record MenuRegistration(
            int legacyGuiId,
            Class<? extends ContainerBase> containerClass,
            Constructor<? extends ContainerBase> constructor,
            RegistryObject<MenuType<ContainerBase>> menuType) {
    }

    private static final class MenuCreationException extends RuntimeException {
        private MenuCreationException(int legacyGuiId, Throwable cause) {
            super("Unable to create menu for GUI id " + legacyGuiId, cause);
        }
    }
}
