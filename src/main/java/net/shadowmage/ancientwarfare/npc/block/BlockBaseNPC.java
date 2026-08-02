package net.shadowmage.ancientwarfare.npc.block;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.block.BlockBase;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;
import net.shadowmage.ancientwarfare.core.render.model.LegacyStateMapperBase;
import net.shadowmage.ancientwarfare.core.util.ModelLoaderHelper;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;

public class BlockBaseNPC extends BlockBase implements IClientRegister {
    public BlockBaseNPC(LegacyMaterial material, String regName) {
        super(material, AncientWarfareNPC.MOD_ID, regName);

        AncientWarfareNPC.proxy.addClientRegister(this);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        final ModelResourceLocation modelLocation = new ModelResourceLocation(new ResourceLocation(AncientWarfareCore.MOD_ID, "npc/" + getRegistryName().getPath()), "normal");
        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return modelLocation;
            }
        });

        ModelLoaderHelper.registerItem(this, "npc", "normal");
    }
}
