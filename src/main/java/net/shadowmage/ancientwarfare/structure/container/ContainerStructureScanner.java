package net.shadowmage.ancientwarfare.structure.container;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import net.shadowmage.ancientwarfare.core.container.ContainerBase;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;
import net.shadowmage.ancientwarfare.core.util.WorldTools;
import net.shadowmage.ancientwarfare.structure.init.AWStructureItems;
import net.shadowmage.ancientwarfare.structure.item.ItemStructureScanner;
import net.shadowmage.ancientwarfare.structure.template.build.validation.StructureValidationType;
import net.shadowmage.ancientwarfare.structure.template.build.validation.StructureValidator;
import net.shadowmage.ancientwarfare.structure.tile.TileStructureScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class ContainerStructureScanner extends ContainerBase {
    private static final String INCLUDE_TAG = "include";
    private static final String VALIDATOR_TAG = "validator";
    private static final String BOUNDS_ACTIVE_TAG = "boundsActive";
    private ItemStack scanner;
    private final InteractionHand hand;
    private final TileStructureScanner scannerTile;
    /**
     * local mirror of the private vanilla listener list so client code can notify listeners directly
     */
    private final List<ContainerListener> listeners = new ArrayList<>();

    public Optional<TileStructureScanner> getScannerTile() {
        return Optional.ofNullable(scannerTile);
    }

    public ContainerStructureScanner(Player player, int x, int y, int z) {
        super(player);
        if (y > 0) {
            scannerTile = WorldTools.getTile(player.level(), new BlockPos(x, y, z), TileStructureScanner.class).orElse(null);
            //noinspection ConstantConditions
            scanner = scannerTile.getScannerInventory().getStackInSlot(0).copy();
            Slot slot = new SlotItemHandler(scannerTile.getScannerInventory(), 0, 8, 8) {
                @Override
                public void setChanged() {
                    super.setChanged();

                    Optional<TileStructureScanner> te = getScannerTile();
                    scanner = te.map(tileStructureScanner -> tileStructureScanner.getScannerInventory().getStackInSlot(0)).orElse(ItemStack.EMPTY);
                    if (player.level().isClientSide) {
                        listeners.forEach(l -> l.slotChanged(ContainerStructureScanner.this, 0, scanner));
                    }
                }
            };

            addSlotToContainer(slot);
            addPlayerSlots();
            hand = null;
        } else {
            scannerTile = null;
            scanner = EntityTools.getItemFromEitherHand(player, ItemStructureScanner.class);
            hand = EntityTools.getHandHoldingItem(player, AWStructureItems.STRUCTURE_SCANNER.get());
        }
    }

    @Override
    public void addSlotListener(ContainerListener listener) {
        super.addSlotListener(listener);
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeSlotListener(ContainerListener listener) {
        super.removeSlotListener(listener);
        listeners.remove(listener);
    }

    @Override
    public void handlePacketData(CompoundTag tag) {
        if (!tag.contains("mode")) {
            return;
        }
        String mode = tag.getString("mode");
        if (mode.equals("export") && ItemStructureScanner.scanStructure(player.level(), scanner) && !getScannerTile().isPresent()) {
            ItemStructureScanner.clearSettings(scanner);
            saveScannerData(player);
        } else if (mode.equals("restore")) {
            getScannerTile().ifPresent(t -> t.restoreTemplate(tag.getString("templateName")));
        } else if (mode.equals("update")) {
            if (tag.contains("name")) {
                updateName(tag.getString("name"));
            } else if (tag.contains(INCLUDE_TAG)) {
                setIncludeImmediately(tag.getBoolean(INCLUDE_TAG));
            } else if (tag.contains(VALIDATOR_TAG)) {
                CompoundTag validatorNBT = tag.getCompound(VALIDATOR_TAG);
                StructureValidationType.getTypeFromName(validatorNBT.getString("validationType")).ifPresent(type -> {
                    StructureValidator validator = type.getValidator();
                    validator.readFromNBT(validatorNBT);
                    setValidator(validator);
                });
            } else if (tag.contains(BOUNDS_ACTIVE_TAG)) {
                setBoundsActive(tag.getBoolean(BOUNDS_ACTIVE_TAG));
            } else if (tag.contains("mods")) {
                updateModDependencies(NBTHelper.getStringSet(tag.getList("mods", Constants.NBT.TAG_STRING)));
            }
        }
    }

    public boolean hasScanner() {
        return !scanner.isEmpty();
    }

    private void sendUpdateData(String name, Tag data) {
        CompoundTag tag = new CompoundTag();
        tag.putString("mode", "update");
        tag.put(name, data);
        sendDataToServer(tag);
    }

    private void saveScannerData(Player player) {
        if (!getScannerTile().isPresent()) {
            player.setItemInHand(hand, scanner);
            return;
        }
        getScannerTile().ifPresent(tile -> {
            tile.getScannerInventory().setStackInSlot(0, scanner);
            tile.setChanged();
        });
    }

    public void export() {
        CompoundTag tag = new CompoundTag();
        tag.putString("mode", "export");
        sendDataToServer(tag);
    }

    public void updateName(String name) {
        //noinspection ConstantConditions
        ItemStructureScanner.setStructureName(scanner, name);
        if (player.level().isClientSide) {
            sendUpdateData("name", StringTag.valueOf(name));
            return;
        }
        saveScannerData(player);
    }

    public Set<String> getModDependencies() {
        return ItemStructureScanner.getModDependencies(scanner);
    }

    public String getName() {
        return ItemStructureScanner.getStructureName(scanner);
    }

    public String getValidationTypeName() {
        return ItemStructureScanner.getValidator(scanner).validationType.getName();
    }

    public void setIncludeImmediately(boolean checked) {
        ItemStructureScanner.setIncludeImmediately(scanner, checked);
        if (player.level().isClientSide) {
            sendUpdateData(INCLUDE_TAG, ByteTag.valueOf((byte) (checked ? 1 : 0)));
            return;
        }
        saveScannerData(player);
    }

    public boolean getIncludeImmediately() {
        return ItemStructureScanner.getIncludeImmediately(scanner);
    }

    public StructureValidator getValidator() {
        return ItemStructureScanner.getValidator(scanner);
    }

    public void setValidator(StructureValidator validator) {
        ItemStructureScanner.setValidator(scanner, validator);
        if (player.level().isClientSide) {
            sendUpdateData(VALIDATOR_TAG, validator.serializeToNBT());
            return;
        }
        saveScannerData(player);
    }

    public void updateValidator(Consumer<StructureValidator> doUpdate) {
        StructureValidator validator = getValidator();
        doUpdate.accept(validator);
        setValidator(validator);
    }

    public void toggleBounds() {
        setBoundsActive(!getBoundsActive());
    }

    private void setBoundsActive(boolean boundsActive) {
        if (player.level().isClientSide) {
            sendUpdateData(BOUNDS_ACTIVE_TAG, ByteTag.valueOf((byte) (boundsActive ? 1 : 0)));
        }
        getScannerTile().ifPresent(t -> t.setBoundsActive(boundsActive));
    }

    public boolean getBoundsActive() {
        return getScannerTile().map(TileStructureScanner::getBoundsActive).orElse(false);
    }

    public boolean getReadyToExport() {
        return ItemStructureScanner.readyToExport(scanner);
    }

    public void restoreTemplate(String name) {
        CompoundTag tag = new CompoundTag();
        tag.putString("mode", "restore");
        tag.putString("templateName", name);
        sendDataToServer(tag);
    }

    public void updateModDependencies(Set<String> mods) {
        //noinspection ConstantConditions
        ItemStructureScanner.setModDependencies(scanner, mods);
        if (player.level().isClientSide) {
            sendUpdateData("mods", NBTHelper.getNBTStringList(mods));
            return;
        }
        saveScannerData(player);
    }
}
