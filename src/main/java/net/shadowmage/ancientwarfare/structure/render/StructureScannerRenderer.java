package net.shadowmage.ancientwarfare.structure.render;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.compat.client.LegacyBlockEntityRenderer;
import net.shadowmage.ancientwarfare.structure.event.IBoxRenderer;
import net.shadowmage.ancientwarfare.structure.item.ItemStructureScanner;
import net.shadowmage.ancientwarfare.structure.tile.TileStructureScanner;

public class StructureScannerRenderer extends LegacyBlockEntityRenderer<TileStructureScanner> {
    @Override
    public void render(TileStructureScanner te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        //lightmap toggling of the 1.12 code is unnecessary - the box renderer draws with position-color shaders that ignore the lightmap
        ItemStack scanner = te.getScannerInventory().getStackInSlot(0);
        if (te.getBoundsActive() && !scanner.isEmpty() && ItemStructureScanner.readyToExport(scanner)) {
            ((IBoxRenderer) scanner.getItem()).renderBox(mc.player, InteractionHand.MAIN_HAND, scanner, partialTicks);
        }
    }

    @Override
    public boolean isGlobalRenderer(TileStructureScanner te) {
        return true;
    }
}
