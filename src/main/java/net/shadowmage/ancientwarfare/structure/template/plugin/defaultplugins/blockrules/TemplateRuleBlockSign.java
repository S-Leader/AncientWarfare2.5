package net.shadowmage.ancientwarfare.structure.template.plugin.defaultplugins.blockrules;

import com.google.gson.JsonParseException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.api.IStructureBuilder;

import javax.annotation.Nullable;

public class TemplateRuleBlockSign extends TemplateRuleVanillaBlocks {
    public static final String PLUGIN_NAME = "vanillaSign";
    private Component[] signContents = emptyLines();
    private int rotation = 0;
    private Tuple<Integer, SignBlockEntity> tileCache;

    public TemplateRuleBlockSign(Level world, BlockPos pos, BlockState state, int turns) {
        super(world, pos, state, turns);
        WorldTools.getTile(world, pos, SignBlockEntity.class).ifPresent(t -> {
            SignText text = t.getFrontText();
            for (int i = 0; i < signContents.length; i++) {
                signContents[i] = text.getMessage(i, false);
            }
        });
        if (state.getBlock() instanceof StandingSignBlock) {
            rotation = (state.getValue(StandingSignBlock.ROTATION) + turns * 4) % 16;
        }
    }

    public TemplateRuleBlockSign() {
        super();
    }

    private static Component[] emptyLines() {
        return new Component[]{Component.empty(), Component.empty(), Component.empty(), Component.empty()};
    }

    private void applyText(SignBlockEntity sign) {
        SignText text = sign.getFrontText();
        for (int i = 0; i < signContents.length; i++) {
            text = text.setMessage(i, signContents[i]);
        }
        sign.setText(text, true);
        sign.setChanged();
    }

    @Override
    public void handlePlacement(Level world, int turns, BlockPos pos, IStructureBuilder builder) {
        if (builder.placeBlock(pos, getState(turns), buildPass)) {
            WorldTools.getTile(world, pos, SignBlockEntity.class).ifPresent(this::applyText);
            BlockTools.notifyBlockUpdate(world, pos);
        }
    }

    @Override
    public boolean shouldReuseRule(Level world, BlockState state, int turns, BlockPos pos) {
        return false;
    }

    @Override
    public void writeRuleData(CompoundTag tag) {
        super.writeRuleData(tag);
        for (int i = 0; i < signContents.length; i++) {
            tag.putString("signContents" + i, Component.Serializer.toJson(signContents[i]));
        }
        tag.putInt("rotation", rotation);
    }

    @Override
    public void parseRule(CompoundTag tag) {
        super.parseRule(tag);
        signContents = emptyLines();
        for (int i = 0; i < signContents.length; i++) {
            String serialized = tag.getString("signContents" + i);
            if (serialized.isEmpty()) {
                signContents[i] = Component.empty();
                continue;
            }
            try {
                Component parsed = Component.Serializer.fromJson(serialized);
                signContents[i] = parsed == null ? Component.empty() : parsed;
            } catch (JsonParseException e) {
                // 1.12 templates stored sign lines as unquoted plain text. Modern
                // Component JSON parsing is strict, so preserve that text literally.
                signContents[i] = Component.literal(serialized);
            }
        }
        rotation = tag.getInt("rotation");
    }

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Nullable
    @Override
    public BlockEntity getTileEntity(int turns) {
        if (tileCache == null || tileCache.getA() != turns) {
            BlockState rotatedState = getState(turns);
            SignBlockEntity sign = new SignBlockEntity(BlockPos.ZERO, rotatedState);
            applyText(sign);
            tileCache = new Tuple<>(turns, sign);
        }
        return tileCache.getB();
    }

    @Override
    public boolean isDynamicallyRendered(int turns) {
        return true;
    }

    @Override
    public BlockState getState(int turns) {
        BlockState rotatedState = super.getState(turns);
        if (rotatedState.getBlock() instanceof StandingSignBlock) {
            rotatedState = rotatedState.setValue(StandingSignBlock.ROTATION, (rotation + turns * 4) % 16);
        }
        return rotatedState;
    }
}
