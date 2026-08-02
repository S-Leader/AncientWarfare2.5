package net.shadowmage.ancientwarfare.automation;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.core.input.InputHandler;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = AncientWarfareAutomation.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AutomationInputHandler {
    private static final String KEYBIND_CATEGORY = "keybind.category.awAutomation";
    private static final String KEY_TOGGLE_WORKBOUNDS_RENDER = "keybind.awAutomation.toggle_workbounds_render";

    public static final KeyMapping TOGGLE_WORKBOUNDS_RENDER = new KeyMapping(KEY_TOGGLE_WORKBOUNDS_RENDER, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F8), KEYBIND_CATEGORY);

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_WORKBOUNDS_RENDER);
    }

    public static void initKeyBindings() {
        InputHandler.registerCallBack(TOGGLE_WORKBOUNDS_RENDER, AutomationInputHandler::toggleWorkboundsConfigValue);
    }

    private static void toggleWorkboundsConfigValue() {
        String newValue = "true";
        if ("true".equals(AWAutomationStatics.renderWorkBounds.get()))
            newValue = "false";
        AWAutomationStatics.renderWorkBounds.set(newValue);

        if (AncientWarfareAutomation.statics.getConfig().hasChanged())
            AncientWarfareAutomation.statics.getConfig().save();
    }
}
