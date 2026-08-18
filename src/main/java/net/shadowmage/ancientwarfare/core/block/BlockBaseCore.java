package net.shadowmage.ancientwarfare.core.block;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;

public abstract class BlockBaseCore extends BlockBase implements IClientRegister, BlockRotationHandler.IRotatableBlock {
    public BlockBaseCore(LegacyMaterial material, String regName) {
        super(material, AncientWarfareCore.MOD_ID, regName);
    }

    @Override
    public BlockRotationHandler.RotationType getRotationType() {
        return BlockRotationHandler.RotationType.FOUR_WAY;
    }

    @Override
    public boolean invertFacing() {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }
}
