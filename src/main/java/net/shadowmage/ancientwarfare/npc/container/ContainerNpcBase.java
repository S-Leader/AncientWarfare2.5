package net.shadowmage.ancientwarfare.npc.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.container.ContainerEntityBase;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

public class ContainerNpcBase<T extends NpcBase> extends ContainerEntityBase<T> {

    public ContainerNpcBase(Player player, int x) {
        super(player, x);
    }

    @Override
    public boolean stillValid(Player var1) {
        return super.stillValid(var1) && var1.distanceToSqr(entity) < 64;
    }

    public void repack() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("repack", true);
        sendDataToServer(tag);
    }

    public void setHome() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("setHome", true);
        sendDataToServer(tag);
    }

    public void clearHome() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("clearHome", true);
        sendDataToServer(tag);
    }

    public void togglefollow() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("togglefollow", true);
        sendDataToServer(tag);
    }
}
