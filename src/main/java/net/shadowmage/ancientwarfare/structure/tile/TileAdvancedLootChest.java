package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.tile.LegacyBlockEntityRegistry;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.structure.util.LootHelper;

import javax.annotation.Nullable;

public class TileAdvancedLootChest extends ChestBlockEntity implements ISpecialLootContainer {
    private static final String LOOT_SETTINGS_TAG = "lootSettings";

    private LootSettings lootSettings = new LootSettings();

    @SuppressWarnings("unchecked")
    public TileAdvancedLootChest() {
        this((BlockEntityType<? extends ChestBlockEntity>) LegacyBlockEntityRegistry.currentType(),
                LegacyBlockEntityRegistry.currentPos(), LegacyBlockEntityRegistry.currentState());
    }

    protected TileAdvancedLootChest(BlockEntityType<? extends ChestBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean fillWithLootAndCheckIfGoodToOpen(@Nullable Player player) {
        return LootHelper.fillWithLootAndCheckIfGoodToOpen(this,
                player != null ? player : EntityTools.findClosestPlayer(level, worldPosition, 100));
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (compound.contains(LOOT_SETTINGS_TAG)) {
            lootSettings = LootSettings.deserializeNBT(compound.getCompound(LOOT_SETTINGS_TAG));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        compound.put(LOOT_SETTINGS_TAG, lootSettings.serializeNBT());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null && tag.contains(LOOT_SETTINGS_TAG)) {
            lootSettings = LootSettings.deserializeNBT(tag.getCompound(LOOT_SETTINGS_TAG));
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put(LOOT_SETTINGS_TAG, lootSettings.serializeNBT());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.contains(LOOT_SETTINGS_TAG)) {
            lootSettings = LootSettings.deserializeNBT(tag.getCompound(LOOT_SETTINGS_TAG));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-1, 0, -1), worldPosition.offset(2, 2, 2));
    }

    public Level getWorld() {
        return level;
    }

    public BlockPos getPos() {
        return worldPosition;
    }

    @Override
    public void setLootSettings(LootSettings settings) {
        this.lootSettings = settings;
        setChanged();
    }

    @Override
    public LootSettings getLootSettings() {
        return lootSettings;
    }
}
