package net.shadowmage.ancientwarfare.core.gui.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.compat.client.ClientSoundHelper;
import net.shadowmage.ancientwarfare.core.gui.GuiContainerBase.ActivationEvent;
import net.shadowmage.ancientwarfare.core.gui.Listener;
import net.shadowmage.ancientwarfare.core.interfaces.ITabCallback;
import net.shadowmage.ancientwarfare.core.util.RenderTools;

/*
 * tab element for use by CompositeTabbed.  Only has minimal self function
 *
 * @author Shadowmage
 */
@OnlyIn(Dist.CLIENT)
public class Tab extends GuiElement {

    ITabCallback parent;
    String label;
    boolean top;

    public Tab(int topLeftX, int topLeftY, boolean top, String label, ITabCallback parentCaller) {
        super(topLeftX, topLeftY);
        this.width = Minecraft.getInstance().font.width(label) + 6;
        this.label = label;
        this.setTooltipIfFound(label);
        this.height = 14;
        this.parent = parentCaller;
        this.top = top;
        this.addNewListener(new Listener(Listener.MOUSE_UP) {
            @Override
            public boolean onEvent(GuiElement widget, ActivationEvent evt) {
                if (visible && enabled && !selected() && isMouseOverElement(evt.mx, evt.my)) {
                    setSelected(true);
                    ClientSoundHelper.playButtonClick();
                    if (parent != null) {
                        parent.onTabSelected(Tab.this);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        if (visible) {
            int y = 162;
            if (selected) {
                y = 138;
            }
            if (!top) {
                y += 48;
            }
            RenderSystem.setShaderTexture(0, widgetTexture1);
            RenderTools.renderQuarteredTexture(256, 256, 152, y, 104, 24, renderX, renderY, width, 16);
            drawStringWithShadow(label, renderX + 3, renderY + 4, 0xffffffff);
        }
    }

}
