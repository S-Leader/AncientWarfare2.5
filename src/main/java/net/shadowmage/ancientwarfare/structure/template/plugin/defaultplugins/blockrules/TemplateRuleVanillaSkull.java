package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;

public class TemplateRuleVanillaSkull extends TemplateRuleBlockTile<BlockEntity> {
    public static final String PLUGIN_NAME = "vanillaSkull";
    private int legacySkullRotation;

    public TemplateRuleVanillaSkull(Level world, BlockPos pos, BlockState state, int turns) {
        super(world, pos, state, turns);
        legacySkullRotation = tag.getInt("Rot");
    }

    public TemplateRuleVanillaSkull() {
        super();
    }

    @Override
    protected Optional<ItemStack> getStack() {
        return Optional.of(new ItemStack(state.getBlock().asItem()));
    }

    @Override
    public void writeRuleData(CompoundTag output) {
        super.writeRuleData(output);
        output.putInt("skullRotation", legacySkullRotation);
    }

    @Override
    public void parseRule(CompoundTag input) {
        super.parseRule(input);
        legacySkullRotation = input.getInt("skullRotation");
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Nullable
    @Override
    public BlockEntity getTileEntity(int turns) {
        return super.getTileEntity(turns);
    }
}
