/**
 * Copyright 2012 John Cummens (aka Shadowmage, Shadowmage4513)
 * This software is distributed under the terms of the GNU General Public License.
 * Please see COPYING for precise license information.
 * <p>
 * This file is part of Ancient Warfare.
 * <p>
 * Ancient Warfare is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Ancient Warfare is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Ancient Warfare.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.shadowmage.ancientwarfare.vehicle.gui.elements;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.gui.elements.Button;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.missiles.IAmmo;
import net.shadowmage.ancientwarfare.vehicle.registry.AmmoRegistry;

@OnlyIn(Dist.CLIENT)
public class ButtonAmmo extends Button {

    IAmmo ammo;
    ItemStack stack;
    VehicleBase vehicle;

    public ButtonAmmo(int topLeftX, int topLeftY, int width, int height, IAmmo ammo, VehicleBase vehicle) {
        super(topLeftX, topLeftY, width, height, "");
        this.ammo = ammo;
        stack = new ItemStack(AmmoRegistry.getItemForAmmo(ammo));
        this.vehicle = vehicle;
        if (ammo != null) {
            setText(I18n.get("item." + ammo.getRegistryName().getPath() + ".name"));
            textX = 24;
        }
    }

    @Override
    protected void onPressed() {
        vehicle.ammoHelper.handleClientAmmoSelection(ammo.getRegistryName());
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTick) {
        super.render(mouseX, mouseY, partialTick);
        if (visible) {
            //draw ammo icon on left
            //draw ammo name to the right of that
            //on the far right, ammo qty
            if (this.ammo != null) {
                String quantity = String.valueOf(vehicle.ammoHelper.getCountOf(ammo));
                Minecraft mc = Minecraft.getInstance();
                GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
                graphics.renderItem(stack, renderX + 3, renderY + 3);
                int quantityRenderX = renderX + this.width - 10 - mc.font.width(quantity);
                drawStringWithShadow(String.valueOf(quantity), quantityRenderX, renderY + textY, 0xffffffff);
            }
        }
    }
}
