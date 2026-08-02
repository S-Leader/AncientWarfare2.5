package net.shadowmage.ancientwarfare.core.owner;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.UUID;

@Immutable
public class Owner {
    public static final Owner EMPTY = new Owner();

    private static final String OWNER_NAME_TAG = "ownerName";
    private static final String OWNER_ID_TAG = "ownerId";
    private final UUID uuid;
    private final String name;

    private Owner() {
        this(new UUID(0, 0), "");
    }

    private Owner(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public Owner(Player player) {
        this(player.getUUID(), player.getGameProfile().getName());
    }

    public Owner(ByteBuf buffer) {
        this(new UUID(buffer.readLong(), buffer.readLong()), new FriendlyByteBuf(buffer).readUtf());
    }

    public Owner(Level world, String name) {
        Player player = world.players().stream().filter(p -> p.getGameProfile().getName().equalsIgnoreCase(name)).findFirst().orElse(null);
        uuid = player != null ? player.getUUID() : new UUID(0, 0);
        this.name = name;
    }

    public boolean isOwnerOrSameTeamOrFriend(@Nullable Entity entity) {
        // check our own implementation of the ownable entities
        if (entity instanceof IOwnable) {
            Owner owner = ((IOwnable) entity).getOwner();
            return isOwnerOrSameTeamOrFriend(entity.level(), owner.getUUID(), owner.getName());
        }
        // check if entity implements vanilla interface if the entity is ownable & player is the owner
        if (entity instanceof OwnableEntity ownable && ownable.getOwner() != null) {
            Entity owner = ownable.getOwner();
            return isOwnerOrSameTeamOrFriend(entity.level(), owner.getUUID(), owner.getName().getString());
        }
        return entity != null && isOwnerOrSameTeamOrFriend(entity.level(), entity.getUUID(), entity.getName().getString());
    }

    public boolean isOwnerOrSameTeamOrFriend(Level world, @Nullable UUID playerId, String playerName) {
        return TeamViewerRegistry.areFriendly(world, uuid, playerId, name, playerName);
    }

    public String getName() {
        return name;
    }

    public UUID getUUID() {
        return uuid;
    }

    public void serializeToBuffer(ByteBuf buffer) {
        buffer.writeLong(uuid.getMostSignificantBits());
        buffer.writeLong(uuid.getLeastSignificantBits());
        new FriendlyByteBuf(buffer).writeUtf(name);
    }

    public CompoundTag serializeToNBT(CompoundTag tag) {
        if (this == EMPTY) {
            return tag;
        }
        tag.putString(OWNER_NAME_TAG, name);
        tag.putUUID(OWNER_ID_TAG, uuid);

        return tag;
    }

    public static Owner deserializeFromNBT(CompoundTag tag) {
        if (tag.contains(OWNER_NAME_TAG)) {
            //noinspection ConstantConditions - CompoundTag has getUniqueId marked as Nullable incorrectly
            return new Owner(tag.hasUUID(OWNER_ID_TAG) ? tag.getUUID(OWNER_ID_TAG) : new UUID(0, 0), tag.getString(OWNER_NAME_TAG));
        }
        return Owner.EMPTY;
    }

    public boolean playerHasCommandPermissions(Level world, UUID playerId, String playerName) {
        return this != Owner.EMPTY && TeamViewerRegistry.areTeamMates(world, uuid, playerId, name, playerName);
    }

}
