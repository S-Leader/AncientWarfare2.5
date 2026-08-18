package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.automation.AncientWarfareAutomation;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.block.BlockBase;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;

public abstract class BlockBaseAutomation extends BlockBase implements IClientRegister {

    public BlockBaseAutomation(LegacyMaterial material, String regName) {
        this(material.properties(), regName);
    }

    protected BlockBaseAutomation(BlockBehaviour.Properties properties, String regName) {
        super(properties, AncientWarfareAutomation.MOD_ID, regName);
        AncientWarfareAutomation.proxy.addClientRegister(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }
}
