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
