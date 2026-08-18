package net.shadowmage.ancientwarfare.npc.tile;


import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.shadowmage.ancientwarfare.core.compat.CapabilityItemHandler;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.interfaces.IInteractableTile;
import net.shadowmage.ancientwarfare.core.interfaces.ITickable;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfare.core.tile.TileOwned;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.npc.config.AWNPCStatics;
import net.shadowmage.ancientwarfare.npc.container.ContainerTownHall;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;
import net.shadowmage.ancientwarfare.npc.item.ItemNpcSpawner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileTownHall extends TileOwned implements IInteractableTile, ITickable, IBlockBreakHandler {

    public boolean alarmActive = false;

    private static final int MAX_NAME_LENGTH = 64;
    private String name = "Unnamed";
    private int broadcastRange = AWNPCStatics.townMaxRange;
    private int updateDelayTicks = 0;
    private boolean isActive = true;
    private boolean isNeglected = false;

    private String oldOwner = null;

    private final List<NpcDeathEntry> deathNotices = new ArrayList<>();

    private final ItemStackHandler inventory = new ItemStackHandler(27) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };

    private final List<ContainerTownHall> viewers = new ArrayList<>();


    public TileTownHall(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void update() {
        if (world == null || world.isClientSide)
            return;

        updateDelayTicks--;
        if (updateDelayTicks <= 0 && isActive) {
            broadcast();
            updateDelayTicks = AWNPCStatics.townUpdateFreq;
        }
    }

    public void addViewer(ContainerTownHall viewer) {
        if (!viewers.contains(viewer)) {
            viewers.add(viewer);
        }
    }

    public void removeViewer(ContainerTownHall viewer) {
        while (viewers.contains(viewer)) {
            viewers.remove(viewer);
        }
    }

    private void broadcast() {
        AABB bb = new AABB(pos.getX() - broadcastRange, pos.getY() - broadcastRange / 2, pos.getZ() - broadcastRange, pos.getX() + broadcastRange + 1, pos.getY() + broadcastRange / 2 + 1, pos.getZ() + broadcastRange + 1);
        List<NpcPlayerOwned> entities = world.getEntitiesOfClass(NpcPlayerOwned.class, bb);
        if (entities.size() > 0) {
            for (Entity entity : entities) {
                if (((NpcPlayerOwned) entity).hasCommandPermissions(getOwner())) {
                    ((NpcPlayerOwned) entity).handleTownHallBroadcast(pos);
                }
            }
        }
    }

    public void clearDeathNotices() {
        deathNotices.clear();
        informViewers();
    }

    public void informViewers() {
        for (ContainerTownHall cth : viewers) {
            cth.onTownHallDeathListUpdated();
        }
    }

    public boolean handleNpcDeath(NpcPlayerOwned npc, DamageSource source) {
        boolean canRes = npc.getDistanceSq(pos) <= (double) broadcastRange * broadcastRange;
        NpcDeathEntry entry = new NpcDeathEntry(npc, source, canRes);
        deathNotices.add(entry);
        informViewers();
        return canRes;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        inventory.deserializeNBT(tag.getCompound("inventory"));
        ListTag entryList = tag.getList("deathNotices", Constants.NBT.TAG_COMPOUND);
        NpcDeathEntry entry;
        for (int i = 0; i < entryList.size(); i++) {
            entry = new NpcDeathEntry(entryList.getCompound(i));
            deathNotices.add(entry);
        }
        if (tag.contains("name"))
            setName(tag.getString("name"));
        if (tag.contains("range"))
            setRange(tag.getInt("range"));
        if (tag.contains("alarmActive"))
            alarmActive = (tag.getBoolean("alarmActive"));
        if (tag.contains("isActive"))
            isActive = (tag.getBoolean("isActive"));
        if (tag.contains("isNeglected"))
            isNeglected = (tag.getBoolean("isNeglected"));
        if (tag.contains("oldOwner"))
            oldOwner = (tag.getString("oldOwner"));
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.put("inventory", inventory.serializeNBT());
        ListTag entryList = new ListTag();
        for (NpcDeathEntry entry : deathNotices) {
            entryList.add(entry.writeToNBT(new CompoundTag()));
        }
        tag.put("deathNotices", entryList);
        tag.putString("name", getName());
        tag.putInt("range", broadcastRange);
        tag.putBoolean("alarmActive", alarmActive);
        tag.putBoolean("isActive", isActive);
        tag.putBoolean("isNeglected", isNeglected);
        if (oldOwner != null)
            tag.putString("oldOwner", oldOwner);
        return tag;
    }

    @Override
    public void onBlockBroken(BlockState state) {
        // Contents and all other persistent town-hall data are carried by the
        // dropped block item through BlockEntityTag, like a shulker box.
    }

    public static class NpcDeathEntry {
        public ItemStack stackToSpawn;
        public String npcType;
        public String npcName;
        public String deathCause;
        public boolean resurrected;
        public boolean canRes;
        public boolean beingResurrected;
        public int[] pos;

        public NpcDeathEntry(CompoundTag tag) {
            readFromNBT(tag);
        }

        public NpcDeathEntry(NpcPlayerOwned npc, DamageSource source, boolean canRes) {
            this.stackToSpawn = ItemNpcSpawner.getSpawnerItemForNpc(npc);
            this.npcType = npc.getNpcFullType();
            this.npcName = npc.hasCustomName() ? npc.getCustomName().getString() : "";
            this.deathCause = source.getMsgId();
            this.canRes = canRes;
            this.pos = new int[]{Mth.floor(npc.getX()), Mth.floor(npc.getY()), Mth.floor(npc.getZ())};
        }

        public final void readFromNBT(CompoundTag tag) {
            stackToSpawn = ItemStack.of(tag.getCompound("spawnerStack"));
            npcType = tag.getString("npcType");
            npcName = tag.getString("npcName");
            deathCause = tag.getString("deathCause");
            resurrected = tag.getBoolean("resurrected");
            canRes = tag.getBoolean("canRes");
            pos = tag.getIntArray("pos");
        }

        public CompoundTag writeToNBT(CompoundTag tag) {
            tag.put("spawnerStack", stackToSpawn.save(new CompoundTag()));
            tag.putString("npcType", npcType);
            tag.putString("npcName", npcName);
            tag.putString("deathCause", deathCause);
            tag.putBoolean("resurrected", resurrected);
            tag.putBoolean("canRes", canRes);
            tag.putIntArray("pos", pos);
            return tag;
        }
    }

    @Override
    public boolean onBlockClicked(Player player, @Nullable InteractionHand hand) {
        if (!player.level().isClientSide && isOwner(player)) {
            AWMenuTypes.open(player, NetworkHandler.GUI_NPC_TOWN_HALL, pos);
        }
        return true;
    }

    public List<NpcDeathEntry> getDeathList() {
        return deathNotices;
    }

    public int getRange() {
        return broadcastRange;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        String normalized = value == null ? "" : value.strip();
        if (normalized.isEmpty()) {
            normalized = "Unnamed";
        }
        name = normalized.substring(0, Math.min(normalized.length(), MAX_NAME_LENGTH));
        markDirty();
    }

    public void setRange(int val) {
        broadcastRange = Mth.clamp(val, 1, AWNPCStatics.townMaxRange);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> inventory).cast();
        }

        return super.getCapability(capability, facing);
    }
}
