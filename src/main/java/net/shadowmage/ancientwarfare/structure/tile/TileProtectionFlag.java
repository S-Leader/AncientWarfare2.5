package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
    public TileProtectionFlag(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private static final String PLAYER_PROFILE_TAG = "playerProfile";
    private static final String OWNER_TAG = "owner";
    private static final float UNBREAKABLE = -1F;
    private Owner owner = Owner.EMPTY;
    private GameProfile playerProfile;

    @Override
    protected void readNBT(CompoundTag tag) {
        super.readNBT(tag);
        owner = Owner.EMPTY;
        playerProfile = null;
        if (tag.contains(OWNER_TAG)) {
            owner = Owner.deserializeFromNBT(tag.getCompound(OWNER_TAG));
            if (tag.contains(PLAYER_PROFILE_TAG)) {
                playerProfile = NbtUtils.readGameProfile(tag.getCompound(PLAYER_PROFILE_TAG));
            }
        }
    }

    @Override
    protected CompoundTag writeNBT(CompoundTag tag) {
        super.writeNBT(tag);
        if (owner != Owner.EMPTY) {
            tag.put(OWNER_TAG, owner.serializeToNBT(new CompoundTag()));
            if (playerProfile != null) {
                tag.put(PLAYER_PROFILE_TAG, NbtUtils.writeGameProfile(new CompoundTag(), playerProfile));
            }
        }
        return tag;
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        StructureMap structureMap = AWGameData.INSTANCE.getPerWorldData(world, StructureMap.class);
        structureMap.getStructureForProtectionFlag(world, pos).ifPresent(structure -> {
            boolean changed = false;
            if (!structure.hasProtectionFlag() || !structure.getProtectionFlagPos().equals(pos)) {
                structure.setProtectionFlagPos(pos);
                changed = true;
            }
            // Repair old saves: the tile owner was persisted, but StructureEntry's
            // conquered/has-flag flags were not. Reconcile them when the tile loads.
            if (isPlayerOwned() && !structure.getConquered()) {
                structure.setConquered();
                changed = true;
            }
            if (changed && !world.isClientSide) {
                NetworkHandler.sendToAllPlayers(new PacketStructureEntry(
                        world.dimension().location().toString(),
                        structure.getChunkX(), structure.getChunkZ(), structure));
            }
        });
    }

    @Override
    public ItemStack getItemStack() {
        ItemStack stack = new ItemStack(AWStructureBlocks.PROTECTION_FLAG.get());
        CompoundTag tag = new CompoundTag();
        writeNBT(tag);
        stack.setTag(tag);
        return stack;
    }

    public void onActivatedBy(Player player) {
        if (isPlayerOwned() || world.isClientSide) {
            return;
        }

        Optional<StructureEntry> structure = AWGameData.INSTANCE.getPerWorldData(world, StructureMap.class)
                .getStructureForProtectionFlag(world, pos);
        if (!structure.isPresent()) {
            return;
        }

        StructureEntry st = structure.get();
        if (ConquerHelper.checkBBConquered(player, st.getBB())) {
            turnOffSoundBlocks(st);
            setOwner(player, player.getGameProfile());
            st.setConquered();
            ConquerHelper.invalidate(world, st.getBB());
            NetworkHandler.sendToAllPlayers(new PacketStructureEntry(
                    world.dimension().location().toString(), st.getChunkX(), st.getChunkZ(), st));
            player.displayClientMessage(Component.translatable("gui.ancientwarfarestructure.structure_conquered", st.getName()), true);
            world.playSound(null, pos, AWStructureSounds.PROTECTION_FLAG_CLAIM, SoundSource.BLOCKS, 1, 1);
        }
        markDirty();
        BlockTools.notifyBlockUpdate(this);
    }

    private void turnOffSoundBlocks(StructureEntry structure) {
        for (BlockPos blockPos : BlockPos.betweenClosed(structure.getBB().min, structure.getBB().max)) {
            if (world.getBlockState(blockPos).getBlock() == AWStructureBlocks.SOUND_BLOCK.get()) {
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
