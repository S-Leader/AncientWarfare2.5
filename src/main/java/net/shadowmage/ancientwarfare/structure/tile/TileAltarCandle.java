package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.world.item.ItemStack;

public class TileAltarCandle extends TileColored {
    public TileAltarCandle(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private static final String FLAME_COLOR_TAG = "flameColor";
    private static final String FLAME_SMOKE_TAG = "flameSmoke";

    private int flameColor = -1;
    private boolean flameSmoke = false;

    public int getFlameColor() {
        return flameColor;
    }

    public boolean isFlameSmoke() {
        return flameSmoke;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setFromStack(ItemStack stack) {
        super.setFromStack(stack);
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag.contains(FLAME_COLOR_TAG)) {
                flameColor = tag.getInt(FLAME_COLOR_TAG);
            }
            if (tag.contains(FLAME_SMOKE_TAG)) {
                flameSmoke = tag.getBoolean(FLAME_SMOKE_TAG);
            }
        }
    }

    @Override
    protected void readNBT(CompoundTag compound) {
        super.readNBT(compound);
        if (compound.contains(FLAME_COLOR_TAG)) {
            flameColor = compound.getInt(FLAME_COLOR_TAG);
        }
        flameSmoke = compound.getBoolean(FLAME_SMOKE_TAG);
    }

    @Override
    protected void writeNBT(CompoundTag compound) {
        super.writeNBT(compound);
        if (flameColor != -1) {
            compound.putInt(FLAME_COLOR_TAG, flameColor);
        }
        if (flameSmoke) {
            compound.putBoolean(FLAME_SMOKE_TAG, true);
        }
    }

    @Override
    public ItemStack getPickBlock() {
        ItemStack stack = super.getPickBlock();
        if (flameColor != -1) {
            stack.getOrCreateTag().put(FLAME_COLOR_TAG, IntTag.valueOf(flameColor));
        }
        if (flameSmoke) {
            stack.getOrCreateTag().put(FLAME_SMOKE_TAG, ByteTag.valueOf((byte) 1));
        }
        return stack;
    }

}
