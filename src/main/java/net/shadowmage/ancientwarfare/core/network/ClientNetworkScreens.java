package net.shadowmage.ancientwarfare.core.network;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;

import java.lang.reflect.InvocationTargetException;

/**
 * Client screen registration for the independently registered native menus.
 */
@OnlyIn(Dist.CLIENT)
public final class ClientNetworkScreens {
    private static boolean registered;

    private ClientNetworkScreens() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        NetworkHandler.INSTANCE.validateClientGuiRegistrations();
        for (AWMenuTypes.MenuRegistration registration : AWMenuTypes.registrations()) {
            registerScreen(registration);
        }
        registered = true;
    }

    private static void registerScreen(AWMenuTypes.MenuRegistration registration) {
        MenuType<ContainerBase> menuType = registration.menuType().get();
        MenuScreens.<ContainerBase, AbstractContainerScreen<ContainerBase>>register(
                menuType, (menu, inventory, title) ->
                        createScreen(menu, registration.legacyGuiId()));
    }

    @SuppressWarnings("unchecked")
    private static AbstractContainerScreen<ContainerBase> createScreen(
            ContainerBase menu, int legacyGuiId) {
        Class<?> guiClass = NetworkHandler.INSTANCE.getGuiClass(legacyGuiId);
        if (guiClass == null) {
            throw new IllegalStateException("No client screen registered for GUI id " + legacyGuiId);
        }
        try {
            AbstractContainerScreen<ContainerBase> screen =
                    (AbstractContainerScreen<ContainerBase>) guiClass
                            .getConstructor(ContainerBase.class)
                            .newInstance(menu);
            NetworkHandler.INSTANCE.flushPendingGuiPackets(menu);
            return screen;
        } catch (Throwable exception) {
            Throwable cause = exception instanceof InvocationTargetException invocation
                    && invocation.getCause() != null ? invocation.getCause() : exception;
            throw new IllegalStateException(
                    "Unable to create screen for GUI id " + legacyGuiId + " from " + guiClass.getName(), cause);
        }
    }
}
