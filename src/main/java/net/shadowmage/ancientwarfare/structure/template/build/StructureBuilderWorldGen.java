package net.shadowmage.ancientwarfare.structure.template.build;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;

public class StructureBuilderWorldGen extends StructureBuilder {

    public StructureBuilderWorldGen(Level world, StructureTemplate template, Direction face, BlockPos pos) {
        super(world, template, face, pos);
    }

    @Override
    public void instantConstruction() {
        template.getValidationSettings().preGeneration(world, buildOrigin, getBuildFace(), template, bb);
        super.instantConstruction();
        template.getValidationSettings().postGeneration(world, buildOrigin, bb, template);
    }

}
