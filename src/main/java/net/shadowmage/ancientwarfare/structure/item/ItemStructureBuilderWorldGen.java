package net.shadowmage.ancientwarfare.structure.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuilder;

public class ItemStructureBuilderWorldGen extends ItemStructureBuilder {
    public ItemStructureBuilderWorldGen(String name) {
        super(name);
    }

    @Override
    protected void buildStructure(Player player, BlockPos hit, Direction facing, StructureBuilder builder) {
        builder.getTemplate().getValidationSettings().preGeneration(player.level(), hit, facing, builder.getTemplate(), builder.getBoundingBox());
        super.buildStructure(player, hit, facing, builder);
        builder.getTemplate().getValidationSettings().postGeneration(player.level(), hit, builder.getBoundingBox(), builder.getTemplate());
    }
}
