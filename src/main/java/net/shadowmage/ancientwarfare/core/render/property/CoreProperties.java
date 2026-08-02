package net.shadowmage.ancientwarfare.core.render.property;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class CoreProperties {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    // Forge 1.13 removed extended/unlisted block-state properties. These aliases keep
    // migration call sites readable while they move to ordinary state/model data.
    public static final DirectionProperty UNLISTED_HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final DirectionProperty UNLISTED_FACING = BlockStateProperties.FACING;
    public static final BooleanProperty VISIBLE = BooleanProperty.create("visible");
}
