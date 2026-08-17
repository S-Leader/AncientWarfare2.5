package net.shadowmage.ancientwarfare.automation.tile.worksite;


import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class WorkSiteFishFarm extends TileWorksiteBoundedInventory {
    private static final int MAX_WATER = 1280;
    private boolean harvestFish = true;
    private boolean harvestInk = true;

    private int waterBlockCount = 0;
    private int waterRescanDelay = 0;

    public WorkSiteFishFarm(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private static final IWorksiteAction FISH_ACTION = WorksiteImplementation::getEnergyPerActivation;

    @Override
    protected Optional<IWorksiteAction> getNextAction() {
        return waterBlockCount > 0 ? Optional.of(FISH_ACTION) : Optional.empty();
    }

    @Override
    protected boolean processAction(IWorksiteAction action) {
        float percentOfMax = ((float) waterBlockCount) / MAX_WATER;
        float check = world.getRandom().nextFloat();
        if (check <= percentOfMax) {
            boolean fish = harvestFish;
            boolean ink = harvestInk;
            if (fish && ink) {
                fish = world.getRandom().nextBoolean();
                ink = !fish;
            }
            if (fish) {
                LootParams lootParams = new LootParams.Builder((ServerLevel) world)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD))
                        .withLuck(getFortune())
                        .create(LootContextParamSets.FISHING);
                List<ItemStack> fishStacks = world.getServer().getLootData().getLootTable(BuiltInLootTables.FISHING).getRandomItems(lootParams);

                if (!InventoryTools.canInventoryHold(mainInventory, fishStacks)) {
                    return false;
                }

                for (ItemStack fishStack : fishStacks) {
                    InventoryTools.insertOrDropItem(mainInventory, fishStack, world, pos);
                }
                return true;
            }
            if (ink) {
                ItemStack inkItem = new ItemStack(Items.INK_SAC);
                int fortune = getFortune();
                if (fortune > 0) {
                    inkItem.grow(world.getRandom().nextInt(fortune + 1));
                }
                if (!InventoryTools.canInventoryHold(mainInventory, inkItem)) {
                    return false;
                }

                InventoryTools.insertOrDropItem(mainInventory, inkItem, world, pos);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean userAdjustableBlocks() {
        return false;
    }

    @Override
    protected void onBoundsSet() {
        super.onBoundsSet();
        BlockPos boundsMax = getWorkBoundsMax();
        setWorkBoundsMax(boundsMax.above(pos.getY() - 1 - boundsMax.getY()));
        boundsMax = getWorkBoundsMin();
        setWorkBoundsMin(boundsMax.above(pos.getY() - 5 - boundsMax.getY()));
        BlockTools.notifyBlockUpdate(this);
    }

    @Override
    public void onBoundsAdjusted() {
        super.onBoundsAdjusted();
        BlockPos boundsMax = getWorkBoundsMax();
        setWorkBoundsMax(boundsMax.above(pos.getY() - 1 - boundsMax.getY()));
        boundsMax = getWorkBoundsMin();
        setWorkBoundsMin(boundsMax.above(pos.getY() - 5 - boundsMax.getY()));
        BlockTools.notifyBlockUpdate(this);
    }

    @Override
    public int getBoundsMaxHeight() {
        return 4;
    }

    public boolean harvestFish() {
        return harvestFish;
    }

    public boolean harvestInk() {
        return harvestInk;
    }

    public void setHarvest(boolean fish, boolean ink) {
        if (harvestFish != fish || harvestInk != ink) {
            harvestFish = fish;
            harvestInk = ink;
            markDirty();
        }
    }

    private void countWater() {
        waterBlockCount = 0;
        BlockPos min = getWorkBoundsMin();
        BlockPos max = getWorkBoundsMax();
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                for (int y = max.getY(); y >= min.getY(); y--) {
                    BlockState state = world.getBlockState(new BlockPos(x, y, z));
                    if (LegacyMaterial.of(state) == LegacyMaterial.WATER) {
                        waterBlockCount++;
                    } else {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        harvestFish = tag.getBoolean("fish");
        harvestInk = tag.getBoolean("ink");
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putBoolean("fish", harvestFish);
        tag.putBoolean("ink", harvestInk);
        return tag;
    }

    @Override
    public WorkType getWorkType() {
        return WorkType.FARMING;
    }

    @Override
    public boolean onBlockClicked(Player player, @Nullable InteractionHand hand) {
        if (!player.level().isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_WORKSITE_FISH_FARM, pos);
        }
        return true;
    }

    @Override
    public void openAltGui(Player player) {
        AWMenuTypes.open(player, NetworkHandler.GUI_WORKSITE_FISH_CONTROL, pos);
    }

    @Override
    protected void updateWorksite() {
        world.getProfiler().push("WaterCount");
        if (waterRescanDelay-- <= 0) {
            countWater();
            waterRescanDelay = 200;
        }
        world.getProfiler().pop();
    }

}
