package net.shadowmage.ancientwarfare.core.datafixes;

import net.minecraft.nbt.CompoundTag;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;

import java.util.UUID;

public class TileOwnerFixer implements ILegacyDataFixer {
    private static final String NEW_OWNER_NAME_TAG = "ownerName";
    private static final String NEW_OWNER_ID_TAG = "ownerId";

    @Override
    public int getFixVersion() {
        return 1;
    }

    @Override
    public CompoundTag fixTagCompound(CompoundTag compound) {
        String id = compound.getString("id");
        try {
            switch (id) {
                case "minecraft:hand_cranked_generator_tile":
                    compound.putString(NEW_OWNER_NAME_TAG, compound.getString("owner"));
                    break;
                case "minecraft:town_hall_tile":
                case "minecraft:auto_crafting_tile":
                case "minecraft:warehouse_control_tile":
                case "minecraft:quarry_tile":
                case "minecraft:forestry_tile":
                case "minecraft:crop_farm_tile":
                case "minecraft:fruit_farm_tile":
                case "minecraft:mushroom_farm_tile":
                case "minecraft:animal_farm_tile":
                case "minecraft:fish_farm_tile":
                case "minecraft:reed_farm_tile":
                    compound.putString(NEW_OWNER_NAME_TAG, compound.getString("owner"));
                    compound.putUUID(NEW_OWNER_ID_TAG, UUID.fromString(compound.getString("ownerId")));
                    break;
                case "minecraft:research_station_tile":
                    compound.putString(NEW_OWNER_NAME_TAG, compound.getString("owningPlayer"));
                    compound.putUUID(NEW_OWNER_ID_TAG, UUID.fromString(compound.getString("ownerId")));
                    break;
                case "minecraft:mailbox_tile":
                case "minecraft:structure_builder_ticked_tile":
                case "minecraft:warehouse_stock_viewer_tile":
                    compound.putUUID(NEW_OWNER_ID_TAG, UUID.fromString(compound.getString("ownerId")));
                    break;
                default:

            }
        } catch (IllegalArgumentException e) {
            int x = compound.getInt("x");
            int y = compound.getInt("y");
            int z = compound.getInt("z");
            AncientWarfareCore.LOG.error("Tile {} at x:{} y:{} z:{} has invalid owner UUID and will need to be removed.", id, x, y, z);
        }
        return compound;
    }

}
