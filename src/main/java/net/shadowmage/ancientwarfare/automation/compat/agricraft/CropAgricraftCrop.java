package net.shadowmage.ancientwarfare.automation.compat.agricraft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.IItemHandler;
import net.shadowmage.ancientwarfare.automation.tile.worksite.cropfarm.ICrop;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/*
 * AgriCraft has no 1.20.1 build on this pack's compile classpath, so the 1.12 API
 * (com.infinityraider.agricraft) is reached reflectively. The original logic is kept
 * intact; if the expected API is absent at runtime this crop handler simply never matches.
 */
public class CropAgricraftCrop implements ICrop {
    private static final String AGRI_BLOCKS_CLASS = "com.infinityraider.agricraft.init.AgriBlocks";
    private static final String TILE_ENTITY_CROP_CLASS = "com.infinityraider.agricraft.tiles.TileEntityCrop";

    private static boolean resolved;
    private static boolean available;
    @Nullable
    private static Object cropBlock;
    @Nullable
    private static Class<?> tileEntityCropClass;
    @Nullable
    private static Method isMatureMethod;
    @Nullable
    private static Method isFertileMethod;
    @Nullable
    private static Method getDropsMethod;
    @Nullable
    private static Method setGrowthStageMethod;

    private static boolean resolveApi() {
        if (resolved) {
            return available;
        }
        resolved = true;
        try {
            Class<?> agriBlocksClass = Class.forName(AGRI_BLOCKS_CLASS);
            Object agriBlocks = agriBlocksClass.getMethod("getInstance").invoke(null);
            cropBlock = agriBlocksClass.getField("CROP").get(agriBlocks);
            tileEntityCropClass = Class.forName(TILE_ENTITY_CROP_CLASS);
            isMatureMethod = tileEntityCropClass.getMethod("isMature");
            isFertileMethod = tileEntityCropClass.getMethod("isFertile");
            getDropsMethod = tileEntityCropClass.getMethod("getDrops", Consumer.class, boolean.class, boolean.class, boolean.class);
            setGrowthStageMethod = tileEntityCropClass.getMethod("setGrowthStage", int.class);
            available = true;
        } catch (ReflectiveOperationException e) {
            AncientWarfareCore.LOG.warn("AgriCraft crop compat disabled - expected API not found: {}", e.toString());
            available = false;
        }
        return available;
    }

    @Nullable
    private static Object getCropTile(Level world, BlockPos pos) {
        if (!resolveApi()) {
            return null;
        }
        BlockEntity te = world.getBlockEntity(pos);
        //noinspection ConstantConditions
        return te != null && tileEntityCropClass.isInstance(te) ? te : null;
    }

    @Override
    public boolean matches(BlockState state) {
        return resolveApi() && state.getBlock() == cropBlock;
    }

    @Override
    public List<BlockPos> getPositionsToHarvest(Level world, BlockPos pos, BlockState state) {
        Object crop = getCropTile(world, pos);

        try {
            if (crop != null && Boolean.TRUE.equals(isMatureMethod.invoke(crop))) {
                return Collections.singletonList(pos);
            }
        } catch (ReflectiveOperationException e) {
            AncientWarfareCore.LOG.error("Error querying AgriCraft crop maturity", e);
        }
        return Collections.emptyList();
    }

    @Override
    public boolean canBeFertilized(BlockState state, Level world, BlockPos pos) {
        Object crop = getCropTile(world, pos);

        try {
            return crop != null && Boolean.TRUE.equals(isFertileMethod.invoke(crop));
        } catch (ReflectiveOperationException e) {
            AncientWarfareCore.LOG.error("Error querying AgriCraft crop fertility", e);
            return false;
        }
    }

    @Override
    public boolean harvest(Level world, BlockState state, BlockPos pos, int fortune, IItemHandler inventory) {
        Object crop = getCropTile(world, pos);

        if (crop == null) {
            return false;
        }

        NonNullList<ItemStack> drops = NonNullList.create();

        try {
            //getting drops and setting stage separately instead of calling onHarvest because of inventory full check
            getDropsMethod.invoke(crop, (Consumer<ItemStack>) drops::add, false, false, true);

            if (!InventoryTools.canInventoryHold(inventory, drops) || !Boolean.TRUE.equals(setGrowthStageMethod.invoke(crop, 0))) {
                return false;
            }
        } catch (ReflectiveOperationException e) {
            AncientWarfareCore.LOG.error("Error harvesting AgriCraft crop", e);
            return false;
        }

        InventoryTools.insertOrDropItems(inventory, drops, world, pos);

        return true;
    }
}
