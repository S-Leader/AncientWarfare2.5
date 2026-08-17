package net.shadowmage.ancientwarfare.automation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.shadowmage.ancientwarfare.automation.render.property.AutomationProperties;
import net.shadowmage.ancientwarfare.automation.tile.torque.TileTorqueSidedCell;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RotationType;
import net.shadowmage.ancientwarfare.core.compat.LegacyMaterial;
import net.shadowmage.ancientwarfare.core.util.LegacyItemStack;
import net.shadowmage.ancientwarfare.core.util.WorldTools;

import java.util.Optional;

public abstract class BlockTorqueTransport extends BlockTorqueBase {
    private static final AABB[] CONNECTION_BOXES = createConnectionBoxes();

    private final TorqueTier fixedTier;

    protected BlockTorqueTransport(String regName) {
        this(regName, null);
    }

    protected BlockTorqueTransport(String regName, TorqueTier fixedTier) {
        super(LegacyMaterial.ROCK, regName);
        this.fixedTier = fixedTier;
        this.setLightOpacity(1);
        if (fixedTier != null) {
            registerDefaultState(defaultBlockState().setValue(AutomationProperties.TIER, fixedTier));
        }
    }

    public final TorqueTier getFixedTier() {
        return fixedTier;
    }

    public final boolean isLegacyVariantBlock() {
        return fixedTier == null;
    }

    @Override
    protected void addProperties(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(AutomationProperties.TIER);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return defaultBlockState().setValue(AutomationProperties.TIER,
                fixedTier == null ? TorqueTier.byMetadata(meta) : fixedTier);
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return fixedTier == null ? state.getValue(AutomationProperties.TIER).getMeta() : 0;
    }

    public final TorqueTier getTier(BlockState state) {
        return fixedTier == null ? state.getValue(AutomationProperties.TIER) : fixedTier;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return fixedTier == null
                ? getStateFromMeta(context.getItemInHand().getDamageValue())
                : defaultBlockState().setValue(AutomationProperties.TIER, fixedTier);
    }

    public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (fixedTier == null) {
            list.add(LegacyItemStack.of(this, 1, 0));
            list.add(LegacyItemStack.of(this, 1, 1));
            list.add(LegacyItemStack.of(this, 1, 2));
        } else {
            list.add(new ItemStack(this));
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, BlockGetter level, BlockPos pos, BlockState state, int fortune) {
        TorqueTier tier = getTier(state);
        Item item = fixedTier == null ? getVariantItem(tier) : asItem();
        if (item != null) {
            drops.add(new ItemStack(item));
        }
    }

    protected abstract Item getVariantItem(TorqueTier tier);

    @Override
    public RotationType getRotationType() {
        return RotationType.SIX_WAY;
    }

    @Override
    public boolean invertFacing() {
        return false;
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter world, BlockPos pos) {
        Optional<TileTorqueSidedCell> te = WorldTools.getTile(world, pos, TileTorqueSidedCell.class);
        if (te.isPresent()) {
            boolean[] sides = te.get().getConnections();
            int mask = 0;
            for (int i = 0; i < Math.min(6, sides.length); i++) {
                if (sides[i]) {
                    mask |= 1 << i;
                }
            }
            return CONNECTION_BOXES[mask];
        }
        return CONNECTION_BOXES[0];
    }

    private static AABB[] createConnectionBoxes() {
        AABB[] boxes = new AABB[64];
        for (int mask = 0; mask < boxes.length; mask++) {
            double minX = (mask & 1 << 4) != 0 ? 0D : 0.1875D;
            double minY = (mask & 1) != 0 ? 0D : 0.1875D;
            double minZ = (mask & 1 << 2) != 0 ? 0D : 0.1875D;
            double maxX = (mask & 1 << 5) != 0 ? 1D : 0.8125D;
            double maxY = (mask & 1 << 1) != 0 ? 1D : 0.8125D;
            double maxZ = (mask & 1 << 3) != 0 ? 1D : 0.8125D;
            boxes[mask] = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }
        return boxes;
    }
}
