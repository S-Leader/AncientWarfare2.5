package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ISpecialLootContainer {
    void setLootSettings(LootSettings settings);

    LootSettings getLootSettings();

    Level getWorld();

    BlockPos getPos();
}
