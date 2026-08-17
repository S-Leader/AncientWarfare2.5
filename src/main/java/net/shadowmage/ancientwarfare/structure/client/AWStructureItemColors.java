package net.shadowmage.ancientwarfare.structure.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import static net.shadowmage.ancientwarfare.structure.init.AWStructureItems.*;

@OnlyIn(Dist.CLIENT)
public class AWStructureItemColors {
    private AWStructureItemColors() {
    }

    public static void init() {
        ItemColors itemColors = Minecraft.getInstance().getItemColors();

        itemColors.register((stack, tintIndex) -> ALTAR_CANDLE.get().getColor(stack), ALTAR_CANDLE.get(), ALTAR_LONG_CLOTH.get(), ALTAR_SHORT_CLOTH.get());
    }
}
