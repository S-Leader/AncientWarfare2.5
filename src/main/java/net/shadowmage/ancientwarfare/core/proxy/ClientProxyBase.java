package net.shadowmage.ancientwarfare.core.proxy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Collects client-side model-data declarations after registries are ready.
 * Static blocks/items use normal JSON models; runtime geometry is installed by
 * DynamicModelRegistry during the standard Forge model-bake event.
 */
@OnlyIn(Dist.CLIENT)
public class ClientProxyBase extends CommonProxyBase {
    private final Set<IClientRegister> clientRegisters = new LinkedHashSet<>();

    public ClientProxyBase() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerAdditionalModels);
    }

    public void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (IClientRegister register : clientRegisters) {
            register.registerClient();
        }
    }

    @Override
    public void addClientRegister(IClientRegister register) {
        clientRegisters.add(register);
    }
}
