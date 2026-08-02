package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.shadowmage.ancientwarfare.core.container.ContainerEntityBase;
import net.shadowmage.ancientwarfare.core.owner.Owner;
import net.shadowmage.ancientwarfare.structure.entity.EntityGate;

public class ContainerGateControl extends ContainerEntityBase<EntityGate> {
    private static final String OWNER_TAG = "owner";

    public ContainerGateControl(Player player, int x, int y, int z) {
        super(player, x);
    }

    @Override
    public void sendInitData() {
        CompoundTag tag = new CompoundTag();
        tag.putString(OWNER_TAG, entity.getOwner().getName());
        sendDataToClient(tag);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (tag.contains("repack")) {
            entity.repackEntity();
        } else if (tag.contains(OWNER_TAG)) {
            String owner = tag.getString(OWNER_TAG);
            entity.setOwner(owner.isEmpty() ? Owner.EMPTY : new Owner(entity.level(), tag.getString(OWNER_TAG)));
            refreshGui();
        }
    }

    public void repackGate() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("repack", true);
        sendDataToServer(tag);
    }

    public void updateOwner(String newOwner) {
        if (!entity.getOwner().getName().equals(newOwner)) {
            CompoundTag tag = new CompoundTag();
            tag.putString(OWNER_TAG, newOwner);
            sendDataToServer(tag);
        }
    }
}
