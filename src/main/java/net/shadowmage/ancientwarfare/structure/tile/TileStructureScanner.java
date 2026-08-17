package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemStackHandler;
import net.shadowmage.ancientwarfare.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;
import net.shadowmage.ancientwarfare.core.util.BlockTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.structure.init.AWStructureItems;
import net.shadowmage.ancientwarfare.structure.item.ItemStructureScanner;
import net.shadowmage.ancientwarfare.structure.item.ItemStructureSettings;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplateManager;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBB;
import net.shadowmage.ancientwarfare.structure.template.build.StructureBuilder;
import net.shadowmage.ancientwarfare.structure.template.scan.TemplateScanner;

import java.util.Collections;
import java.util.Optional;

public class TileStructureScanner extends TileUpdatable implements IBlockBreakHandler {
    public TileStructureScanner(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private static final String SCANNER_INVENTORY_TAG = "scannerInventory";
    private static final String BOUNDS_ACTIVE_TAG = "boundsActive";
    private static final String FACING_TAG = "facing";

    private ItemStackHandler scannerInventory = new ItemStackHandler(1) {
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return stack.getItem() == AWStructureItems.STRUCTURE_SCANNER.get() ? super.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);

            if (!world.isClientSide) {
                BlockTools.notifyBlockUpdate(TileStructureScanner.this);
            }
        }
    };

    private boolean boundsActive = true;
    private Direction facing = Direction.NORTH;
    private Direction renderFacing = Direction.NORTH;

    public ItemStackHandler getScannerInventory() {
        return scannerInventory;
    }

    @Override
    protected void writeUpdateNBT(CompoundTag tag) {
        super.writeUpdateNBT(tag);
        tag.put(SCANNER_INVENTORY_TAG, scannerInventory.serializeNBT());
        tag.putBoolean(BOUNDS_ACTIVE_TAG, boundsActive);
        tag.putByte(FACING_TAG, (byte) facing.ordinal());
    }

    private void updateRenderFacing() {
        ItemStack scanner = getScannerInventory().getStackInSlot(0);
        Direction newRenderFacing = scanner.getItem() == AWStructureItems.STRUCTURE_SCANNER.get() &&
                ItemStructureScanner.readyToExport(scanner)
                ? Direction.UP : facing;

        if (newRenderFacing != renderFacing) {
            renderFacing = newRenderFacing;
            BlockTools.notifyBlockUpdate(this);
        }
    }

    @Override
    protected void handleUpdateNBT(CompoundTag tag) {
        super.handleUpdateNBT(tag);
        scannerInventory.deserializeNBT(tag.getCompound(SCANNER_INVENTORY_TAG));
        boundsActive = tag.getBoolean(BOUNDS_ACTIVE_TAG);
        facing = Direction.values()[tag.getByte(FACING_TAG)];
        updateRenderFacing();
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag compound) {
        CompoundTag tag = super.writeToNBT(compound);
        tag.put(SCANNER_INVENTORY_TAG, scannerInventory.serializeNBT());
        tag.putBoolean(BOUNDS_ACTIVE_TAG, boundsActive);
        tag.putByte(FACING_TAG, (byte) facing.ordinal());
        return tag;
    }

    @Override
    public void readFromNBT(CompoundTag compound) {
        super.readFromNBT(compound);
        scannerInventory.deserializeNBT(compound.getCompound(SCANNER_INVENTORY_TAG));
        boundsActive = compound.getBoolean(BOUNDS_ACTIVE_TAG);
        facing = Direction.values()[compound.getByte(FACING_TAG)];
    }

    public boolean getBoundsActive() {
        return boundsActive;
    }

    public void setBoundsActive(boolean boundsActive) {
        this.boundsActive = boundsActive;
    }

    public void setFacing(Direction facing) {
        this.facing = facing;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getRenderBoundingBox() {
        ItemStack scanner = scannerInventory.getStackInSlot(0);
        if (scanner.getItem() != AWStructureItems.STRUCTURE_SCANNER.get()) {
            return super.getRenderBoundingBox();
        }

        ItemStructureSettings settings = ItemStructureSettings.getSettingsFor(scanner);

        if (!settings.hasPos1() || !settings.hasPos2()) {
            return super.getRenderBoundingBox();
        }

        return settings.getBoundingBox().inflate(1, 0, 1);
    }

    public Direction getRenderFacing() {
        return renderFacing;
    }

    public void restoreTemplate(String name) {
        ItemStack scanner = scannerInventory.getStackInSlot(0);

        if (scanner.getItem() != AWStructureItems.STRUCTURE_SCANNER.get()) {
            return;
        }

        StructureTemplateManager.getTemplate(name).ifPresent(template -> {
            ItemStructureSettings settings = ItemStructureSettings.getSettingsFor(scanner);
            if (ItemStructureScanner.readyToExport(scanner)) {
                int turns = (6 - settings.face().get2DDataValue()) % 4;
                StructureTemplate dummyTemplate = TemplateScanner.scan(world, Collections.emptySet(), settings.getMin(), settings.getMax(), settings.buildKey(), turns, "dummy");
                if (isSameTemplateSizeAndOffset(template, dummyTemplate)) {
                    setMainTemplateSettings(name, scanner, template);
                    restoreTemplate(template, settings.getBoundingBox(), settings.buildKey(), settings.face());
                    return;
                }
            }

            saveToScannerItemAndRestoreTemplate(name, scanner, template, settings);
        });
    }

    private void setMainTemplateSettings(String name, ItemStack scanner, StructureTemplate template) {
        ItemStructureScanner.setStructureName(scanner, name);
        ItemStructureScanner.setValidator(scanner, template.getValidationSettings());
        ItemStructureScanner.setModDependencies(scanner, template.modDependencies);
    }

    private void saveToScannerItemAndRestoreTemplate(String name, ItemStack scanner, StructureTemplate template, ItemStructureSettings settings) {
        Direction placementFacing = facing.getOpposite();
        BlockPos key = pos.relative(placementFacing, template.getSize().getZ() - template.getOffset().getZ());
        StructureBB bb = new StructureBB(key, placementFacing, template);
        settings.setBuildKey(key, placementFacing);
        settings.setName(name);
        settings.setPos1(bb.min);
        settings.setPos2(bb.max);
        setMainTemplateSettings(name, scanner, template);
        ItemStructureSettings.setSettingsFor(scanner, settings);

        restoreTemplate(template, settings.getBoundingBox(), key, placementFacing);
    }

    private void restoreTemplate(StructureTemplate template, AABB boundingBox, BlockPos buildPos, Direction face) {
        clearBoundingBox(boundingBox);
        buildTemplate(template, buildPos, face);
        clearItemsOnGround(boundingBox);
    }

    private void clearItemsOnGround(AABB boundingBox) {
        world.getEntitiesOfClass(ItemEntity.class, boundingBox).forEach(Entity::discard);
    }

    private void buildTemplate(StructureTemplate template, BlockPos buildPos, Direction face) {
        StructureBuilder builder = new StructureBuilder(world, template, face, buildPos);
        builder.instantConstruction();
    }

    private void clearBoundingBox(AABB boundingBox) {
        clearEntities(boundingBox);
        clearBlocks(boundingBox);
    }

    private void clearBlocks(AABB boundingBox) {
        BlockPos.betweenClosed((int) boundingBox.minX, (int) boundingBox.minY, (int) boundingBox.minZ,
                (int) boundingBox.maxX, (int) boundingBox.maxY, (int) boundingBox.maxZ).forEach(blockPos -> world.removeBlock(blockPos, false));
    }

    private void clearEntities(AABB boundingBox) {
        AABB expandedBoundingBox = new AABB(boundingBox.minX, boundingBox.minY, boundingBox.minZ, boundingBox.maxX + 1, boundingBox.maxY + 1, boundingBox.maxZ + 1);
        world.getEntitiesOfClass(Entity.class, expandedBoundingBox).forEach(Entity::discard);
    }

    private boolean isSameTemplateSizeAndOffset(StructureTemplate template, StructureTemplate dummyTemplate) {
        return dimensionsAreSame(template, dummyTemplate) && offsetIsSame(template, dummyTemplate);
    }

    private boolean offsetIsSame(StructureTemplate template, StructureTemplate dummyTemplate) {
        return template.getOffset().equals(dummyTemplate.getOffset());
    }

    private boolean dimensionsAreSame(StructureTemplate template, StructureTemplate dummyTemplate) {
        return template.getSize().equals(dummyTemplate.getSize());
    }

    @Override
    public void onBlockBroken(BlockState state) {
        InventoryTools.dropItemsInWorld(world, scannerInventory, pos);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        ScannerTracker.registerScanner(this);
    }

    public void export() {
        getScanner().ifPresent(scanner -> ItemStructureScanner.scanStructure(world, scanner));
    }

    void reloadMainSettings() {
        getScanner().ifPresent(scanner -> {
                    String name = ItemStructureScanner.getStructureName(scanner);
                    StructureTemplateManager.getTemplate(name).ifPresent(template -> setMainTemplateSettings(name, scanner, template));
                    markDirty();
                }
        );
    }

    Optional<ItemStack> getScanner() {
        ItemStack scanner = scannerInventory.getStackInSlot(0);

        if (scanner.getItem() != AWStructureItems.STRUCTURE_SCANNER.get()) {
            return Optional.empty();
        }
        return Optional.of(scanner);
    }
}
