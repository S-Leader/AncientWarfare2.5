package net.shadowmage.ancientwarfare.structure.block;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.proxy.IClientRegister;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.AncientWarfareStructure;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.render.ParticleOnlyModel;
import net.shadowmage.ancientwarfare.structure.render.RenderItemAdvancedLootChest;
import net.shadowmage.ancientwarfare.structure.tile.TileAdvancedLootChest;

import javax.annotation.Nullable;

/**
 * Vanilla chest behavior with Ancient Warfare's deferred loot settings.
 */
public class BlockAdvancedLootChest extends ChestBlock implements IClientRegister, net.shadowmage.ancientwarfare.core.util.ILegacyRegistryName {
    private static final ResourceLocation ID =
            new ResourceLocation(AncientWarfareStructure.MOD_ID, "advanced_loot_chest");

    public BlockAdvancedLootChest() {
        super(BlockBehaviour.Properties.copy(Blocks.CHEST), AWStructureBlocks.ADVANCED_LOOT_CHEST_TILE::get);
        AncientWarfareStructure.proxy.addClientRegister(this);
    }

    /**
     * Transitional accessor used by the remaining legacy model code and registration.
     */
    @Override
    public ResourceLocation getRegistryName() {
        return ID;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return AWStructureBlocks.ADVANCED_LOOT_CHEST_TILE.get().create(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            boolean mayOpen = WorldTools.getTile(level, pos, TileAdvancedLootChest.class)
                    .map(tile -> tile.fillWithLootAndCheckIfGoodToOpen(player)).orElse(false);
            if (!mayOpen) {
                return InteractionResult.CONSUME;
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        // Models are loaded normally from 1.20 blockstates/models JSON.
    }
}
