package net.shadowmage.ancientwarfare.npc.datafixes;

import net.minecraft.nbt.CompoundTag;
import net.shadowmage.ancientwarfare.core.datafixes.ILegacyDataFixer;
import net.shadowmage.ancientwarfare.npc.skin.NpcSkinSettings;

public class NpcSkinFixer implements ILegacyDataFixer {
    @Override
    public int getFixVersion() {
        return 9;
    }

    @Override
    public CompoundTag fixTagCompound(CompoundTag compound) {
        String id = compound.getString("id");

        if (id.startsWith("ancientwarfarenpc")) {
            String customTex = compound.getString("customTex");
            if (!customTex.isEmpty()) {
                NpcSkinSettings skinSettings = new NpcSkinSettings();
                if (customTex.startsWith("Player:")) {
                    skinSettings.setSkinType(NpcSkinSettings.SkinType.PLAYER);
                    skinSettings.setPlayerName(customTex.replace("Player:", ""));
                } else {
                    skinSettings.setSkinType(NpcSkinSettings.SkinType.NPC_TYPE);
                    skinSettings.setNpcTypeName(customTex);
                }
                compound.put("skinSettings", skinSettings.serializeNBT());
            }
        }

        return compound;
    }
}
