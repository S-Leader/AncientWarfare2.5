package net.shadowmage.ancientwarfare.automation.render.property;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.shadowmage.ancientwarfare.automation.block.TorqueTier;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelProperty;

public class AutomationProperties {
    public static final LegacyModelProperty<Boolean> ACTIVE = LegacyModelProperty.create("active", false);
    public static final LegacyModelProperty<Boolean> DYNAMIC = LegacyModelProperty.create("dynamic", false);
    public static final LegacyModelProperty<Float>[] ROTATIONS = new LegacyModelProperty[6];
    public static final LegacyModelProperty<Boolean> USE_INPUT = LegacyModelProperty.create("use_input", false);
    public static final LegacyModelProperty<Float> INPUT_ROTATION = LegacyModelProperty.create("input_rotation", 0.0F);
    public static final EnumProperty<TorqueTier> TIER = EnumProperty.create("tier", TorqueTier.class);
    public static final LegacyModelProperty<Boolean> IS_CONTROL = LegacyModelProperty.create("is_control", false);
    public static final LegacyModelProperty<Integer> HEIGHT = LegacyModelProperty.create("height", 0, value -> value >= 0 && value <= 30);
    public static final LegacyModelProperty<Integer> WIDTH = LegacyModelProperty.create("width", 0, value -> value >= 0 && value <= 30);
    public static final LegacyModelProperty<Float> ROTATION = LegacyModelProperty.create("rotation", 0.0F);

    static {
        for (Direction facing : Direction.values()) {
            ROTATIONS[facing.get3DDataValue()] = LegacyModelProperty.create("rotation_" + facing.name().toLowerCase(), 0.0F);
        }
    }
}
