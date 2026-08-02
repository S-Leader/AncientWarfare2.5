package net.shadowmage.ancientwarfare.core.gui;

import net.shadowmage.ancientwarfare.core.container.ContainerBackpack;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;

public class GuiBackpack extends GuiContainerBase<ContainerBackpack> {

    public GuiBackpack(ContainerBase container) {
        super(container, 178, 192);
        this.ySize = this.getContainer().guiHeight;
    }

    @Override
    public void initElements() {
    }

    @Override
    public void setupElements() {
    }

    @Override
    protected boolean checkHotbarKeyPressed(int keyCode, int scanCode)//this code handles whether to allow the backpack to be moved from its slot via the number keys
    {
        boolean callSuper = true;
        for (int slot = 0; slot < 9; slot++) {
            if (this.mc.options.keyHotbarSlots[slot].matches(keyCode, scanCode) && slot == getContainer().backpackSlotIndex) {
                callSuper = false;
            }
        }
        return callSuper && super.checkHotbarKeyPressed(keyCode, scanCode);
    }

}
