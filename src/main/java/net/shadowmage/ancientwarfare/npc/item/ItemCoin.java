package net.shadowmage.ancientwarfare.npc.item;

import com.google.common.collect.ImmutableMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.StringTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.npc.init.AWNPCItems;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.init.AWStructureSounds;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemCoin extends ItemBaseNPC {
    private static final Map<CoinMetal, Block> METALS_TO_BLOCKS = ImmutableMap.of(
            CoinMetal.ANCIENT, AWStructureBlocks.COIN_STACK_ANCIENT.get(),
            CoinMetal.GOLD, AWStructureBlocks.COIN_STACK_GOLD.get(),
            CoinMetal.SILVER, AWStructureBlocks.COIN_STACK_SILVER.get(),
            CoinMetal.COPPER, AWStructureBlocks.COIN_STACK_COPPER.get()
    );

    public ItemCoin() {
        super("coin");
    }

    public void getSubItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        Arrays.stream(CoinMetal.values()).forEach(metal -> {
                    ItemStack subItem = getCoinStack(metal);
                    items.add(subItem);
                }
        );
    }

    private static ItemStack getCoinStack(CoinMetal metal) {
        return getCoinStack(metal, 1);
    }

    public static ItemStack getCoinStack(CoinMetal metal, int stackSize) {
        ItemStack coinStack = new ItemStack(AWNPCItems.COIN.get());
        coinStack.getOrCreateTag().put("metal", StringTag.valueOf(metal.getName()));
        coinStack.setCount(stackSize);
        return coinStack;
    }

    public static boolean isSpecificCoin(ItemStack stack, CoinMetal coinMetal) {
        return stack.getItem() == AWNPCItems.COIN.get() && getMetal(stack) == coinMetal;
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack) + "." + getMetalName(stack);
    }

    public static CoinMetal getMetal(ItemStack stack) {
        if (!stack.hasTag()) {
            return CoinMetal.COPPER;
        }
        return CoinMetal.byName(getMetalName(stack));
    }

    private static String getMetalName(ItemStack stack) {
        //noinspection ConstantConditions
        return stack.hasTag() ? stack.getTag().getString("metal") : "";
    }

    @Override
    public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getItemInHand(hand);
        CoinMetal metal = getMetal(stack);

        if ((stack.getCount() < 8)) {
            return InteractionResult.FAIL;
        }

        BlockState state = world.getBlockState(pos);

        if (!state.canBeReplaced()) {
            pos = pos.relative(facing);
        }

        Block coinBlock = METALS_TO_BLOCKS.get(metal);

        BlockState placeState = coinBlock.defaultBlockState();
        if (!world.getBlockState(pos).canBeReplaced() || !placeState.canSurvive(world, pos)
                || !world.isUnobstructed(placeState, pos, CollisionContext.of(player))) {
            return InteractionResult.FAIL;
        }
        world.setBlock(pos, coinBlock.defaultBlockState(), 3);
        stack.shrink(8);
        world.playSound(null, pos, AWStructureSounds.COIN_STACK_INTERACT, SoundSource.PLAYERS, 0.5f, 1);
        return InteractionResult.SUCCESS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
        if (getMetal(stack).equals(CoinMetal.ANCIENT)) {
            tooltip.add(I18n.get("item.coin.ancient.tooltip", ChatFormatting.DARK_AQUA.toString() + ChatFormatting.ITALIC.toString()));
        }
    }

    public enum CoinMetal {
        ANCIENT("ancient", 0x445948),
        GOLD("gold", 0xFFD700),
        SILVER("silver", 0xC0C0C0),
        COPPER("copper", 0xB87333);

        private String name;
        private int color;

        CoinMetal(String name, int color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        private static Map<String, CoinMetal> values = new HashMap<>();

        static {
            Arrays.stream(values()).forEach(m -> values.put(m.getName(), m));
        }

        public static CoinMetal byName(String name) {
            return values.get(name);
        }

        public int getColor() {
            return color;
        }
    }
}
