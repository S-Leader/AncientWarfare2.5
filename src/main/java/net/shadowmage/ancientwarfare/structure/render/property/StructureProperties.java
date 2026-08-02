package net.shadowmage.ancientwarfare.structure.render.property;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class StructureProperties {
    public static final EnumProperty<TopBottomPart> TOP_BOTTOM_PART = EnumProperty.create("part", TopBottomPart.class);
    public static final EnumProperty<Direction.Axis> AXIS = EnumProperty.create("axis", Direction.Axis.class);
}
