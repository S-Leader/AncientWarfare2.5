package net.shadowmage.ancientwarfare.structure.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.gamedata.AWGameData;
import net.shadowmage.ancientwarfare.core.network.PacketBase;
import net.shadowmage.ancientwarfare.core.util.PacketHelper;
import net.shadowmage.ancientwarfare.core.util.StringTools;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureEntry;
import net.shadowmage.ancientwarfare.structure.gamedata.StructureMap;

public class PacketStructureEntry extends PacketBase {
    private String dimension;
    private int cx;
    private int cz;
    private StructureEntry entry;

    public PacketStructureEntry() {
    }

    public PacketStructureEntry(String dimension, int cx, int cz, StructureEntry entry) {
        this.dimension = dimension;
        this.cx = cx;
        this.cz = cz;
        this.entry = entry;
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        StringTools.writeString(data, dimension);
        data.writeInt(cx);
        data.writeInt(cz);
        CompoundTag entryTag = new CompoundTag();
        entry.writeToNBT(entryTag);
        PacketHelper.writeNBTTag(data, entryTag);
    }

    @Override
    protected void readFromStream(ByteBuf data) {
        dimension = StringTools.readString(data);
        cx = data.readInt();
        cz = data.readInt();
        entry = new StructureEntry();
        entry.readFromNBT(PacketHelper.readNBTTag(data));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void execute() {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.dimension().location().toString().equals(dimension)) {
            // Client cache uses the same data object, but never marks entries unique.
            AWGameData.INSTANCE.getPerWorldData(level, StructureMap.class)
                    .setGeneratedAt(dimension, cx, cz, entry, false, false);
        }
    }
}
