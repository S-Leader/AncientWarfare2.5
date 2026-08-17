package net.shadowmage.ancientwarfare.structure.item;


import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.input.IItemKeyInterface;
import net.shadowmage.ancientwarfare.core.input.InputHandler;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.gui.GuiLootChestPlacer;
import net.shadowmage.ancientwarfare.structure.registry.EntitySpawnNBTRegistry;
import net.shadowmage.ancientwarfare.structure.tile.ISpecialLootContainer;
import net.shadowmage.ancientwarfare.structure.tile.LootSettings;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ItemLootChestPlacer extends ItemBaseStructure implements IItemKeyInterface {
    private static final String LOOT_SETTINGS_TAG = "lootSettings";

    private static final Map<String, LootContainerInfo> LOOT_CONTAINERS = new LinkedHashMap<>();
    private static final String LOOT_CONTAINER_NAME_TAG = "lootContainerName";

    public static Map<String, LootContainerInfo> getLootContainers() {
        return LOOT_CONTAINERS;
    }

    public static void registerLootContainer(String name, ItemStack stack, LootContainerInfo.IPlacementChecker mayPlace) {
        LOOT_CONTAINERS.put(name, new LootContainerInfo(name, stack, mayPlace));
    }

    public ItemLootChestPlacer() {
        super("loot_chest_placer");
        setMaxStackSize(1);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        if (!player.level().isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_LOOT_CHEST_PLACER, 0, 0, 0);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        ItemStack lootChestPlacer = EntityTools.getItemFromEitherHand(player, ItemLootChestPlacer.class);
        if (lootChestPlacer.isEmpty()) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        if (player.level().isClientSide) {
            return;
        }

        Entity entity = event.getTarget();

        LootSettings lootSettings = getLootSettings(lootChestPlacer).orElse(new LootSettings());

        lootSettings.setSpawnEntity(true);
        //noinspection ConstantConditions
        lootSettings.setEntity(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()));
        lootSettings.setEntityNBT(EntitySpawnNBTRegistry.getEntitySpawnNBT(entity));
        setLootSettings(lootChestPlacer, lootSettings);
        entity.discard();

        player.sendSystemMessage(Component.translatable("guistrings.spawner.entity_set", entity.getName()));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
        String keyText = InputHandler.ALT_ITEM_USE_1.getTranslatedKeyMessage().getString();
        String text = keyText + " = " + I18n.get("guistrings.structure.loot_placer.copy");
        tooltip.add(text);

        keyText = InputHandler.ALT_ITEM_USE_2.getTranslatedKeyMessage().getString();
        text = keyText + " = " + I18n.get("guistrings.structure.loot_placer.paste");
        tooltip.add(text);

    }

    @Override
    public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack placer = player.getItemInHand(hand);
        Optional<LootSettings> lootSettings = getLootSettings(placer);
        if (!lootSettings.isPresent()) {
            return InteractionResult.PASS;
        }

        BlockPos placePos = pos.relative(facing);
        LootContainerInfo lootContainerInfo = getLootContainerInfo(placer);
        ItemStack itemBlockStack = lootContainerInfo.getStack().copy();
        BlockItem itemBlock = (BlockItem) itemBlockStack.getItem();
        Block block = itemBlock.getBlock();
        if (lootContainerInfo.canPlace(block, world, placePos, facing, player)) {
            Vec3 hitLocation = new Vec3(pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ);
            BlockHitResult hitResult = new BlockHitResult(hitLocation, facing, pos, false);
            BlockPlaceContext placementContext = new BlockPlaceContext(new UseOnContext(player, hand, hitResult)) {
                @Override
                public ItemStack getItemInHand() {
                    return itemBlockStack;
                }
            };
            InteractionResult result = itemBlock.place(placementContext);
            if (result.consumesAction()) {
                WorldTools.getTile(world, placementContext.getClickedPos(), ISpecialLootContainer.class)
                        .ifPresent(tile -> lootSettings.get().transferToContainer(tile));
                return result;
            }
        }
        return InteractionResult.FAIL;
    }

    public static LootContainerInfo getLootContainerInfo(ItemStack placer) {
        if (placer.hasTag()) {
            CompoundTag compound = placer.getTag();
            //noinspection ConstantConditions
            if (compound.contains(LOOT_CONTAINER_NAME_TAG)) {
                return LOOT_CONTAINERS.getOrDefault(compound.getString(LOOT_CONTAINER_NAME_TAG), getFirstLootContainer());
            }
        }
        return getFirstLootContainer();
    }

    public static void setContainerName(ItemStack placer, String name) {
        placer.getOrCreateTag().put(LOOT_CONTAINER_NAME_TAG, StringTag.valueOf(name));
    }

    private static LootContainerInfo getFirstLootContainer() {
        return LOOT_CONTAINERS.values().iterator().next();
    }

    public static Optional<LootSettings> getLootSettings(ItemStack placer) {
        //noinspection ConstantConditions
        return placer.hasTag() && placer.getTag().contains(LOOT_SETTINGS_TAG) ?
                Optional.of(LootSettings.deserializeNBT(placer.getTag().getCompound(LOOT_SETTINGS_TAG))) : Optional.empty();
    }

    public static void setLootSettings(ItemStack placer, LootSettings lootSettings) {
        placer.getOrCreateTag().put(LOOT_SETTINGS_TAG, lootSettings.serializeNBT());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        super.registerClient();
    }

    @Override
    public boolean onKeyActionClient(Player player, ItemStack stack, ItemAltFunction altFunction) {
        return altFunction == ItemAltFunction.ALT_FUNCTION_1 || altFunction == ItemAltFunction.ALT_FUNCTION_2;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onKeyAction(Player player, ItemStack placer, ItemAltFunction altFunction) {
        BlockPos hit = BlockTools.getBlockClickedOn(player, player.level(), false);
        WorldTools.getTile(player.level(), hit, ISpecialLootContainer.class).ifPresent(te -> {
            if (altFunction == ItemAltFunction.ALT_FUNCTION_1) {
                BlockState state = player.level().getBlockState(hit);
                getLootContainerInfoByStack(state.getBlock().getCloneItemStack(state,
                        new BlockHitResult(Vec3.atCenterOf(hit), Direction.UP, hit, false), player.level(), hit, player))
                        .ifPresent(container -> setContainerName(placer, container.getName()));
                getLootSettings(placer).ifPresent(s -> setLootSettings(placer, s.transferFromContainer(te)));
            } else if (altFunction == ItemAltFunction.ALT_FUNCTION_2) {
                getLootSettings(placer).ifPresent(s -> s.transferToContainer(te));
                BlockTools.notifyBlockUpdate(player.level(), hit);
            }
        });
    }

    private Optional<LootContainerInfo> getLootContainerInfoByStack(ItemStack stack) {
        for (LootContainerInfo container : LOOT_CONTAINERS.values()) {
            if (ItemStack.isSameItemSameTags(container.getStack(), stack)) {
                return Optional.of(container);
            }
        }
        return Optional.empty();
    }

    public static class LootContainerInfo {
        public static final IPlacementChecker SINGLE_BLOCK_PLACEMENT_CHECKER = (block, world, pos, sidePlacedOn, placer) -> world.getBlockState(pos).canBeReplaced() && block.defaultBlockState().canSurvive(world, pos);
        private final String name;
        private final ItemStack stack;
        private final IPlacementChecker placementChecker;

        private LootContainerInfo(String name, ItemStack stack, IPlacementChecker placementChecker) {
            this.name = name;
            this.stack = stack;
            this.placementChecker = placementChecker;
        }

        public String getName() {
            return name;
        }

        public ItemStack getStack() {
            return stack;
        }

        private boolean canPlace(Block block, Level world, BlockPos pos, Direction sidePlacedOn, Player placer) {
            return placementChecker.mayPlace(block, world, pos, sidePlacedOn, placer);
        }

        public interface IPlacementChecker {
            boolean mayPlace(Block block, Level world, BlockPos pos, Direction sidePlacedOn, Player placer);
        }
    }
}
