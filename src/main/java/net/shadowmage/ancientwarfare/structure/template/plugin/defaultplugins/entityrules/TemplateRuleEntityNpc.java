package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.entityrules;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

public class TemplateRuleEntityNpc extends TemplateRuleEntity<NpcBase> {
    public static final String PLUGIN_NAME = "AWNpc";

    public TemplateRuleEntityNpc() {
        super();
    }

    public TemplateRuleEntityNpc(Level world, NpcBase entity, int turns, int x, int y, int z) {
        super(world, entity, turns, x, y, z);
    }

    @Override
    protected void updateEntityOnPlacement(int turns, BlockPos pos, NpcBase e) {
        super.updateEntityOnPlacement(turns, pos, e);
        e.setHomeAreaAtCurrentPosition();
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }
}
