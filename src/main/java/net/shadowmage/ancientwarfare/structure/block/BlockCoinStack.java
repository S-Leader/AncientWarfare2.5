package net.shadowmage.ancientwarfare.structure.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.AncientWarfareCore;
import net.shadowmage.ancientwarfare.core.compat.CapabilityItemHandler;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.render.model.LegacyModelLoader;
import net.shadowmage.ancientwarfare.core.render.model.LegacyStateMapperBase;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.NBTBuilder;
import net.shadowmage.ancientwarfare.npc.item.ItemCoin;
import net.shadowmage.ancientwarfare.structure.init.AWStructureSoundTypes;
import net.shadowmage.ancientwarfare.structure.init.AWStructureSounds;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlockCoinStack extends BlockBaseStructure {
    private ItemCoin.CoinMetal coinMetal;

    public BlockCoinStack(String regName, ItemCoin.CoinMetal coinMetal) {
        super(LegacyMaterial.GROUND, regName);
        setHardness(0.3F);
        setResistance(0.4F);
        this.coinMetal = coinMetal;
        registerDefaultState(defaultBlockState().setValue(STACK_SIZE, 8));
    }

    private static final Map<Integer, AABB> STACK_SIZE_AABBs = new ImmutableMap.Builder<Integer, AABB>()
            .put(8, new AABB(0D, 0D, 0D, 1D, 0.10D, 1D))
            .put(16, new AABB(0D, 0D, 0D, 1D, 0.25D, 1D))
            .put(24, new AABB(0D, 0D, 0D, 1D, 0.37D, 1D))
            .put(32, new AABB(0D, 0D, 0D, 1D, 0.57D, 1D))
            .put(40, new AABB(0D, 0D, 0D, 1D, 0.81D, 1D))
            .put(48, new AABB(0D, 0D, 0D, 1D, 0.81D, 1D))
            .put(56, new AABB(0D, 0D, 0D, 1D, 0.95D, 1D))
            .put(64, new AABB(0D, 0D, 0D, 1D, 1.00D, 1D))
            .build();

    private static final String STACK_SIZE_TAG = "size";

    private static final PropertyStackSize STACK_SIZE = new PropertyStackSize();

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STACK_SIZE);
    }

    public void getSubBlocks(CreativeModeTab itemIn, NonNullList<ItemStack> items) {
        for (int stackSize = 8; stackSize < 65; stackSize = stackSize + 8) {
            ItemStack stack = new ItemStack(this.asItem());
            stack.setTag(new NBTBuilder().setInteger(STACK_SIZE_TAG, stackSize).build());
            items.add(stack);
        }
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(STACK_SIZE, (meta + 1) * 8);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return (state.getValue(STACK_SIZE) / 8) - 1;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(BlockState state) {
        return false;
    }

    @Override
    public void onBlockPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }
        worldIn.setBlock(pos, state.setValue(STACK_SIZE, getCoinStackSize(stack)), 3);
    }

    private int getCoinStackSize(ItemStack stack) {
        //noinspection ConstantConditions
        return stack.hasTag() ? stack.getTag().getInt(STACK_SIZE_TAG) : 0;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter world, BlockPos pos, BlockState state, int fortune) {
        drops.add(ItemCoin.getCoinStack(coinMetal, state.getValue(STACK_SIZE)));
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide) {
            return true;
        }
        if (ItemCoin.isSpecificCoin(stack, coinMetal)) {
            addToStack(world, pos, state, player, stack);
        } else {
            removeFromStack(world, pos, state, player);
        }
        return true;
    }

    private void removeFromStack(Level world, BlockPos pos, BlockState state, Player player) {
        int blockstacksize = state.getValue(STACK_SIZE);

        //noinspection ConstantConditions
        InventoryTools.insertOrDropItem(player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElseThrow(NullPointerException::new), ItemCoin.getCoinStack(coinMetal, 8), world, pos);
        if (blockstacksize > 8) {
            world.setBlock(pos, state.setValue(STACK_SIZE, blockstacksize - 8), 3);
        } else {
            world.removeBlock(pos, false);
        }
        world.playSound(null, pos, AWStructureSounds.COIN_STACK_INTERACT, SoundSource.PLAYERS, 0.5f, 1);
    }

    private void addToStack(Level world, BlockPos pos, BlockState state, Player player, ItemStack stack) {
        if (stack.getCount() >= 8) {
            int blockStackSize = state.getValue(STACK_SIZE);
            if (blockStackSize < 64) {
                world.setBlock(pos, state.setValue(STACK_SIZE, (blockStackSize + 8)), 3);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(8);
                }
            }
        }
        world.playSound(null, pos, AWStructureSounds.COIN_STACK_INTERACT, SoundSource.PLAYERS, 0.5f, 1);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, @Nullable Entity entity) {
        return AWStructureSoundTypes.COINSTACK;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable Level worldIn, List<String> tooltip, TooltipFlag flagIn) {
        tooltip.add(getCoinStackSize(stack) + " " + I18n.get("guistrings.structure.coins"));
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return STACK_SIZE_AABBs.get(state.getValue(STACK_SIZE));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void registerClient() {
        //noinspection ConstantConditions
        ResourceLocation baseLocation = new ResourceLocation(AncientWarfareCore.MOD_ID, "structure/" + getRegistryName().getPath());

        LegacyModelLoader.setCustomStateMapper(this, new LegacyStateMapperBase() {
            @Override
            @OnlyIn(Dist.CLIENT)
            protected ModelResourceLocation getModelResourceLocation(BlockState state) {
                return new ModelResourceLocation(baseLocation, getPropertyString(state.getValues()));
            }
        });

        String modelPropInteger = "size=%s";

        LegacyModelLoader.setCustomMeshDefinition(this.asItem(), stack -> {
            if (!stack.hasTag()) {
                return new ModelResourceLocation(baseLocation, String.format(modelPropInteger, 8));
            }
            //noinspection ConstantConditions
            return new ModelResourceLocation(baseLocation, String.format(modelPropInteger, stack.getTag().getInt(STACK_SIZE_TAG)));
        });

        for (int stackSize = 8; stackSize < 65; stackSize = stackSize + 8) {
            LegacyModelLoader.registerItemVariants(this.asItem(),
                    new ModelResourceLocation(baseLocation, String.format(modelPropInteger, stackSize)));
        }
    }

    public static class PropertyStackSize extends Property<Integer> {
        private final ImmutableSet<Integer> allowedValues;

        private PropertyStackSize() {
            super(STACK_SIZE_TAG, Integer.class);
            this.allowedValues = ImmutableSet.of(8, 16, 24, 32, 40, 48, 56, 64);
        }

        @Override
        public Collection<Integer> getPossibleValues() {
            return this.allowedValues;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            } else if (other instanceof PropertyStackSize && super.equals(other)) {
                PropertyStackSize propertyinteger = (PropertyStackSize) other;
                return this.allowedValues.equals(propertyinteger.allowedValues);
            } else {
                return false;
            }
        }

        @Override
        public int generateHashCode() {
            return 31 * super.generateHashCode() + this.allowedValues.hashCode();
        }

        @SuppressWarnings({"Guava", "squid:S4738"})
        @Override
        public Optional<Integer> getValue(String value) {
            try {
                Integer integer = Integer.valueOf(value);
                return this.allowedValues.contains(integer) ? Optional.of(integer) : Optional.empty();
            } catch (NumberFormatException var3) {
                return Optional.empty();
            }
        }

        public String getName(Integer value) {
            return value.toString();
        }
    }
}
