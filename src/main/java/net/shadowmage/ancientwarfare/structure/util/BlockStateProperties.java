package net.shadowmage.ancientwarfare.structure.util;

import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.shadowmage.ancientwarfare.structure.block.WoodVariant;

public class BlockStateProperties {
    public static final EnumProperty<WoodVariant> VARIANT = EnumProperty.create("variant", WoodVariant.class);

    private BlockStateProperties() {
    }

}
