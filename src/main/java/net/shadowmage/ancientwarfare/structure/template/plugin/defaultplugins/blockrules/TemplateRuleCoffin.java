package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.structure.block.BlockCoffin;
import net.shadowmage.ancientwarfare.structure.tile.TileCoffin;

public class TemplateRuleCoffin extends TemplateRuleMulti<TileCoffin> {
    public static final String PLUGIN_NAME = "coffin";

    public TemplateRuleCoffin(Level world, BlockPos pos, BlockState state, int turns) {
        super(world, pos, state, turns, TileCoffin.class);
        BlockCoffin.CoffinDirection direction = BlockCoffin.CoffinDirection.fromName(tag.getString("direction"));
        tag.putString("direction", rotateDirection(direction, turns).getName());
    }

    private BlockCoffin.CoffinDirection rotateDirection(BlockCoffin.CoffinDirection direction, int turns) {
        BlockCoffin.CoffinDirection ret = direction;
        for (int turn = 0; turn < turns; turn++) {
            ret = ret.rotateY();
        }
        return ret;
    }

    public TemplateRuleCoffin() {
        super(TileCoffin.class);
    }

    @Override
    protected void rotateTe(BlockEntity te, int turns) {
        super.rotateTe(te, turns);
        if (te instanceof TileCoffin coffin) {
            coffin.setDirection(rotateDirection(coffin.getDirection(), turns));
        }
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
