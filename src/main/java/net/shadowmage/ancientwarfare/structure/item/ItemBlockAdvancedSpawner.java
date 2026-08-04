package net.shadowmage.ancientwarfare.structure.item;


import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.input.IItemKeyInterface;
import net.shadowmage.ancientwarfare.core.item.ItemBlockBase;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.tile.SpawnerSettings;
import net.shadowmage.ancientwarfare.structure.tile.SpawnerSettings.EntitySpawnGroup;
import net.shadowmage.ancientwarfare.structure.tile.SpawnerSettings.EntitySpawnSettings;
import net.shadowmage.ancientwarfare.structure.tile.TileAdvancedSpawner;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockAdvancedSpawner extends ItemBlockBase implements IItemKeyInterface {
    private static final String SPAWNER_SETTINGS_TAG = "spawnerSettings";

    public ItemBlockAdvancedSpawner(Block block) {
        super(block);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        ItemStack stack = context.getItemInHand();
        CompoundTag rootTag = stack.getOrCreateTag();
        if (!SpawnerSettings.containsSpawnerConfiguration(rootTag)) {
            SpawnerSettings settings = SpawnerSettings.getDefaultSettings();
            CompoundTag defaultTag = new CompoundTag();
            settings.writeToNBT(defaultTag);
            rootTag.put(SPAWNER_SETTINGS_TAG, defaultTag);
        }
        return super.place(context);
    }

    @Override
    protected void onBlockPlaced(BlockPlaceContext context, BlockState placedState) {
        if (!context.getLevel().isClientSide) {
            ItemStack stack = context.getItemInHand();
            WorldTools.getTile(context.getLevel(), context.getClickedPos(), TileAdvancedSpawner.class).ifPresent(tile -> {
                SpawnerSettings settings = new SpawnerSettings();
                settings.readFromNBT(stack.getOrCreateTag());
                if (settings.getSpawnGroups().isEmpty()) {
                    settings = SpawnerSettings.getDefaultSettings();
                }
                tile.setSettings(settings);
            });
        }
    }

    @Override
    public boolean onKeyActionClient(Player player, ItemStack stack, ItemAltFunction altFunction) {
        return altFunction == ItemAltFunction.ALT_FUNCTION_1;
    }

    @Override
    public void onKeyAction(Player player, ItemStack stack, ItemAltFunction altFunction) {
        if (player.isShiftKeyDown()) {
            AWMenuTypes.open(player, NetworkHandler.GUI_SPAWNER_ADVANCED_INVENTORY, 0, 0, 0);
        } else {
            AWMenuTypes.open(player, NetworkHandler.GUI_SPAWNER_ADVANCED, 0, 0, 0);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level world, List<String> tooltip, TooltipFlag flagIn) {
        //noinspection ConstantConditions
        if (!stack.hasTag() || !SpawnerSettings.containsSpawnerConfiguration(stack.getTag())) {
            tooltip.add(I18n.get("guistrings.corrupt_item"));
            return;
        }
        SpawnerSettings tooltipSettings = new SpawnerSettings();
        tooltipSettings.readFromNBT(stack.getTag());
        List<EntitySpawnGroup> groups = tooltipSettings.getSpawnGroups();
        tooltip.add(I18n.get("guistrings.spawner.group_count") + ": " + groups.size());
        EntitySpawnGroup group;
        for (int i = 0; i < groups.size(); i++) {
            group = groups.get(i);
            tooltip.add(I18n.get("guistrings.spawner.group_number") + ": " + (i + 1) + " " + I18n.get("guistrings.spawner.group_weight") + ": " + group.getWeight());
            for (EntitySpawnSettings set : group.getEntitiesToSpawn()) {
                tooltip.add("  " + I18n.get("guistrings.spawner.entity_type") + ": " + I18n.get(set.getEntityName()) + " " + set.getSpawnMin() + " to " + set.getSpawnMax() + " (" + (set.getSpawnTotal() < 0 ? "infinite" : set.getSpawnTotal()) + " total)");
            }
        }
    }
}
