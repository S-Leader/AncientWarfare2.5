package net.shadowmage.ancientwarfare.core.gui.options;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.shadowmage.ancientwarfare.core.config.ConfigManager;

import java.util.List;

/**
 * Registers the Ancient Warfare options page with Forge's modern Mods screen.
 */
@OnlyIn(Dist.CLIENT)
public final class OptionsGuiFactory {
    private static boolean registered;

    private OptionsGuiFactory() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(OptionsGuiFactory::createScreen));
    }

    private static Screen createScreen(Minecraft minecraft, Screen parent) {
        return new OptionsScreen(parent);
    }

    private static final class OptionsScreen extends Screen {
        private static final int BUTTON_WIDTH = 260;
        private final Screen parent;
        private List<ConfigManager.BooleanEntry> entries = List.of();

        private OptionsScreen(Screen parent) {
            super(Component.translatable("awconfig.config_name"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            entries = ConfigManager.getBooleanEntries();
            int x = (width - BUTTON_WIDTH) / 2;
            int firstY = 42;
            int visibleRows = Math.max(1, (height - 90) / 24);
            int count = Math.min(entries.size(), visibleRows);

            for (int i = 0; i < count; i++) {
                ConfigManager.BooleanEntry entry = entries.get(i);
                Button button = Button.builder(message(entry), pressed -> {
                    entry.toggle();
                    pressed.setMessage(message(entry));
                }).bounds(x, firstY + i * 24, BUTTON_WIDTH, 20).build();
                addRenderableWidget(button);
            }

            addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, ignored -> onClose())
                    .bounds((width - 200) / 2, height - 32, 200, 20)
                    .build());
        }

        private static Component message(ConfigManager.BooleanEntry entry) {
            Component value = Component.translatable(entry.get() ? "options.on" : "options.off");
            return Component.translatable(entry.translationKey()).append(": ").append(value);
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics);
            graphics.drawCenteredString(font, title, width / 2, 16, 0xFFFFFF);
            if (entries.isEmpty()) {
                graphics.drawCenteredString(font, Component.translatable("awconfig.no_client_options"),
                        width / 2, height / 2 - 10, 0xA0A0A0);
            }
            super.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void onClose() {
            minecraft.setScreen(parent);
        }
    }
}
