package net.shadowmage.ancientwarfare.structure.tile;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;

import javax.annotation.Nullable;

public abstract class TileFlag extends TileUpdatable {
    private static final String NAME_TAG = "name";
    private String name = "";

    public boolean isPlayerOwned() {
        return false;
    }

    @Nullable
    public GameProfile getPlayerProfile() {
        return null;
    }

    public String getName() {
        return name;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return new AABB(pos, pos.offset(1, 3, 1));
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        readNBT(tag);
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        readNBT(compound);
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        writeNBT(tag);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag compound) {
        return writeNBT(super.writeToNBT(compound));
    }

    protected void readNBT(CompoundTag tag) {
        name = tag.getString(NAME_TAG);
    }

    protected CompoundTag writeNBT(CompoundTag tag) {
        tag.putString(NAME_TAG, name);
        return tag;
    }

    @SuppressWarnings("ConstantConditions")
    public void setFromStack(ItemStack stack) {
        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            readNBT(tag);
        }
    }

    public abstract ItemStack getItemStack();
}
