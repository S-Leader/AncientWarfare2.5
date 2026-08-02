package net.shadowmage.ancientwarfare.npc.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.shadowmage.ancientwarfare.core.network.PacketBase;
import net.shadowmage.ancientwarfare.npc.item.IExtendedReachWeapon;

import java.io.IOException;

public class PacketExtendedReachAttack extends PacketBase {
    private int entityId;

    public PacketExtendedReachAttack() {
    }

    public PacketExtendedReachAttack(int entityId) {
        this.entityId = entityId;
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        data.writeInt(entityId);
    }

    @Override
    protected void readFromStream(ByteBuf data) throws IOException {
        entityId = data.readInt();
    }

    @Override
    protected void execute(Player player) {
        Entity entity = player.level().getEntity(entityId);
        if (entity == null) {
            return;
        }
        Item heldItem = player.getMainHandItem().getItem();
        if (!(heldItem instanceof IExtendedReachWeapon) || player.distanceTo(entity) > ((IExtendedReachWeapon) heldItem).getReach()) {
            return;
        }
        player.attack(entity);
    }
}
