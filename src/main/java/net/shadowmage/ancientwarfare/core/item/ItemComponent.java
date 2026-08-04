package net.shadowmage.ancientwarfare.core.item;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;

/**
 * A single, registry-backed machine component. 1.20 no longer has item
 * metadata subtypes, so every gear/bearing/shaft owns a real item id.
 */
public final class ItemComponent extends ItemBase implements IClientRegister {
    /** Legacy metadata values retained only for source compatibility. */
    @Deprecated public static final int WOODEN_GEAR_SET = 0;
    @Deprecated public static final int IRON_GEAR_SET = 1;
    @Deprecated public static final int STEEL_GEAR_SET = 2;
    @Deprecated public static final int WOODEN_BEARINGS = 3;
    @Deprecated public static final int IRON_BEARINGS = 4;
    @Deprecated public static final int STEEL_BEARINGS = 5;
    @Deprecated public static final int WOODEN_TORQUE_SHAFT = 6;
    @Deprecated public static final int IRON_TORQUE_SHAFT = 7;
    @Deprecated public static final int STEEL_TORQUE_SHAFT = 8;

    private final String modelName;

    public ItemComponent(String registryName, String modelName) {
        super(AncientWarfareCore.MOD_ID, registryName);
        this.modelName = modelName;
        AncientWarfareCore.proxy.addClientRegister(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        ModelLoaderHelper.registerItem(this, 0, "automation/" + modelName + "#inventory");
    }
}
