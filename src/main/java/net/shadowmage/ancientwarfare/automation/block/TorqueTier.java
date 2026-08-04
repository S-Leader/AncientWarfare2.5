package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

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
        if (meta < 0 || meta >= values().length) {
            return LIGHT;
        }
        return values()[meta];
    }
    public static TorqueTier fromItemStack(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof BlockTorqueTransport transport && transport.getFixedTier() != null) {
                return transport.getFixedTier();
            }
            if (block instanceof BlockFlywheelController controller && controller.getFixedTier() != null) {
                return controller.getFixedTier();
            }
            if (block instanceof BlockFlywheelStorage storage && storage.getFixedTier() != null) {
                return storage.getFixedTier();
            }
        }
        return byMetadata(stack.getDamageValue());
    }

}
