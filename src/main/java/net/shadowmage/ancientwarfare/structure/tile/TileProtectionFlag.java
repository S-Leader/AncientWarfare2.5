package net.shadowmage.ancientwarfare.structure.tile;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.owner.Owner;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureEntry;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureMap;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.init.AWStructureSounds;
import net.shadowmage.ancientwarfare.structure.network.PacketStructureEntry;
import net.shadowmage.ancientwarfare.structure.util.ConquerHelper;

import javax.annotation.Nullable;
import java.util.Optional;

public class TileProtectionFlag extends TileFlag {
    private static final String PLAYER_PROFILE_TAG = "playerProfile";
    private static final String OWNER_TAG = "owner";
    private static final float UNBREAKABLE = -1F;
    private Owner owner = Owner.EMPTY;
    private GameProfile playerProfile;

    @Override
    protected void readNBT(CompoundTag tag) {
        super.readNBT(tag);
        if (tag.contains(OWNER_TAG)) {
            owner = Owner.deserializeFromNBT(tag.getCompound(OWNER_TAG));
            playerProfile = NbtUtils.readGameProfile(tag.getCompound(PLAYER_PROFILE_TAG));
        }
    }

    @Override
    protected CompoundTag writeNBT(CompoundTag tag) {
        super.writeNBT(tag);
        if (owner != Owner.EMPTY) {
            tag.put(OWNER_TAG, owner.serializeToNBT(new CompoundTag()));
            tag.put(PLAYER_PROFILE_TAG, NbtUtils.writeGameProfile(new CompoundTag(), playerProfile));
        }
        return tag;
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        AWGameData.INSTANCE.getPerWorldData(world, StructureMap.class).getStructureAt(world, pos).ifPresent(structure -> {
            if (!structure.getProtectionFlagPos().equals(pos)) {
                structure.setProtectionFlagPos(pos);
                if (!world.isClientSide) {
                    NetworkHandler.sendToAllPlayers(new PacketStructureEntry(world.dimension().location().toString(), structure.getChunkX(), structure.getChunkZ(), structure));
                }
            }
        });
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack stack = new ItemStack(AWStructureBlocks.PROTECTION_FLAG);
        CompoundTag tag = new CompoundTag();
        writeNBT(tag);
        stack.setTag(tag);
        return stack;
    }

    public void onActivatedBy(Player player) {
        if (isPlayerOwned() || world.isClientSide) {
            return;
        }

        Optional<StructureEntry> structure = AWGameData.INSTANCE.getPerWorldData(world, StructureMap.class).getStructureAt(world, pos);
        if (!structure.isPresent()) {
            return;
        }

        StructureEntry st = structure.get();
        if (ConquerHelper.checkBBConquered(player, st.getBB())) {
            turnOffSoundBlocks(st);
            setOwner(player, player.getGameProfile());
            st.setConquered();
            player.displayClientMessage(Component.translatable("gui.ancientwarfarestructure.structure_conquered", st.getName()), true);
            world.playSound(null, pos, AWStructureSounds.PROTECTION_FLAG_CLAIM, SoundSource.BLOCKS, 1, 1);
        }
        markDirty();
        BlockTools.notifyBlockUpdate(this);
    }

    private void turnOffSoundBlocks(StructureEntry structure) {
        for (BlockPos blockPos : BlockPos.betweenClosed(structure.getBB().min, structure.getBB().max)) {
            if (world.getBlockState(blockPos).getBlock() == AWStructureBlocks.SOUND_BLOCK) {
                WorldTools.getTile(world, blockPos, TileSoundBlock.class).ifPresent(TileSoundBlock::turnOffByProtectionFlag);
            }
        }
    }

    private void setOwner(Player player, GameProfile playerProfile) {
        owner = new Owner(player);
        this.playerProfile = playerProfile;
    }

    public boolean isPlayerOwned() {
        return owner != Owner.EMPTY;
    }

    @Nullable
    public GameProfile getPlayerProfile() {
        return playerProfile;
    }

    public float getPlayerRelativeBlockHardness(Player player, float original) {
        return owner.isOwnerOrSameTeamOrFriend(player) ? original : UNBREAKABLE;
    }

    public boolean shouldProtectAgainst(Player player) {
        return !owner.isOwnerOrSameTeamOrFriend(player);
    }
}
