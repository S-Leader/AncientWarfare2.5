package net.shadowmage.ancientwarfare.core.util.parsing;

import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class MultiBlockStateMatcher implements Predicate<BlockState> {
    private BlockStateMatcher[] blockStateMatchers;

    public MultiBlockStateMatcher(BlockStateMatcher... blockStateMatchers) {
        this.blockStateMatchers = blockStateMatchers;
    }

    @Override
    public boolean test(BlockState state) {
        for (BlockStateMatcher blockStateMatcher : blockStateMatchers) {
            if (blockStateMatcher.test(state)) {
                return true;
            }
        }

        return false;
    }
}
