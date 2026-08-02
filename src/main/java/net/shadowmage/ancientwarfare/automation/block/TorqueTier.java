package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.util.StringRepresentable;

public enum TorqueTier implements StringRepresentable {
    LIGHT(0), MEDIUM(1), HEAVY(2);

    private int meta;

    TorqueTier(int meta) {
        this.meta = meta;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }

    public int getMeta() {
        return meta;
    }

    public static TorqueTier byMetadata(int meta) {
        return values()[meta];
    }
}
