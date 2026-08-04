package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelProperty;

public abstract class BlockTorqueTransportSided extends BlockTorqueTransport {
    public static final LegacyModelProperty<Boolean>[] CONNECTIONS = new LegacyModelProperty[6];

    static {
        for (Direction facing : Direction.values()) {
            CONNECTIONS[facing.ordinal()] = LegacyModelProperty.create("connection_" + facing.name().toLowerCase(), false);
        }
    }

    protected BlockTorqueTransportSided(String regName) {
        super(regName);
    }

    protected BlockTorqueTransportSided(String regName, TorqueTier fixedTier) {
        super(regName, fixedTier);
    }

    @Override
    protected void addProperties(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.addProperties(builder);
    }

}
