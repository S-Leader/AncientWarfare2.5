package net.shadowmage.ancientwarfare.core.compat.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * GLFW-backed replacement for LWJGL2's org.lwjgl.input.Mouse helpers.
 */
public final class ClientMouseHelper {
    private ClientMouseHelper() {
    }

    public static double x() {
        return Minecraft.getInstance().mouseHandler.xpos();
    }

    public static double y() {
        return Minecraft.getInstance().mouseHandler.ypos();
    }

    public static void setPosition(double x, double y) {
        Minecraft minecraft = Minecraft.getInstance();
        GLFW.glfwSetCursorPos(minecraft.getWindow().getWindow(), x, y);
    }
}
