package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.block.BlockBase;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;

public class BlockBaseStructure extends BlockBase implements IClientRegister {
    public BlockBaseStructure(LegacyMaterial material, String regName) {
        super(material, AncientWarfareStructure.MOD_ID, regName);
        AncientWarfareStructure.proxy.addClientRegister(this);
    }

    protected BlockBaseStructure(BlockBehaviour.Properties properties, String regName) {
        super(properties, AncientWarfareStructure.MOD_ID, regName);
        AncientWarfareStructure.proxy.addClientRegister(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }
}
