package net.shadowmage.ancientwarfare.core.util.parsing;

import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class PropertyStateMatcher implements Predicate<BlockState> {
    private PropertyState propertyState;

    public PropertyStateMatcher(PropertyState propertyState) {
        this.propertyState = propertyState;
    }

    @Override
    public boolean test(BlockState iBlockState) {
        return iBlockState.getValue(propertyState.getProperty()).equals(propertyState.getValue());
    }
}
