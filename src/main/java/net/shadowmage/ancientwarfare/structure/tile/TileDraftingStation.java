package net.shadowmage.ancientwarfare.structure.tile;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.shadowmage.ancientwarfare.core.interfaces.ITickable;
import net.shadowmage.ancientwarfare.core.tile.IBlockBreakHandler;
import net.shadowmage.ancientwarfare.core.tile.TileUpdatable;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.NBTHelper;
import net.shadowmage.ancientwarfare.structure.init.AWStructureBlocks;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplate;
import net.shadowmage.ancientwarfare.structure.template.StructureTemplateManager;

import java.util.List;
import java.util.stream.Collectors;

public class TileDraftingStation extends TileUpdatable implements ITickable, IBlockBreakHandler {
    private static final String STRUCTURE_NAME_TAG = "structureName";
    private String structureName;//structure pulled from live structure list anytime a ref is needed
    private boolean isStarted;//has started compiling resources -- will need input to cancel
    private List<StructureTemplate.BuildResource> buildResources = NonNullList.create();
    private boolean isFinished;//is finished compiling resources, awaiting output-slot availability
    private int remainingTime;//not really time, but raw item count
    private int totalTime;//total raw-item count

    public ItemStackHandler inputSlots = new ItemStackHandler(27) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };

    public ItemStackHandler outputSlot = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };

    @Override
    public void update() {
        if (!hasWorld() || world.isClientSide) {
            return;
        }
        if (structureName != null && !StructureTemplateManager.getTemplate(structureName).isPresent()) {
            stopCurrentWork();
        }
        if (structureName == null || !isStarted) {
            return;
        }
        if (!isFinished && tryRemoveResource()) {
            isFinished = true;
        }
        if (isFinished && tryFinish()) {
            stopCurrentWork();
        }
    }

    private boolean tryRemoveResource() {
        for (int slot = 0; slot < inputSlots.getSlots(); slot++) {
            ItemStack inventoryStack = inputSlots.getStackInSlot(slot);
            if (!inventoryStack.isEmpty() && removeBuildResource(inventoryStack)) {
                inventoryStack.shrink(1);
                if (inventoryStack.isEmpty()) {
                    inputSlots.setStackInSlot(slot, ItemStack.EMPTY);
                }
                break;
            }
        }
        return buildResources.isEmpty();
    }

    private boolean removeBuildResource(ItemStack inventoryStack) {
        for (int i = 0; i < buildResources.size(); i++) {
            StructureTemplate.BuildResource buildResource = buildResources.get(i);
            if (InventoryTools.doItemStacksMatchRelaxed(buildResource.getStackRequired(), inventoryStack)) {
                ItemStack returnStack = buildResource.shrinkStackRequiredAndGetRemaining();
                if (!returnStack.isEmpty()) {
                    InventoryTools.insertOrDropItem(inputSlots, returnStack, world, pos.above());
                }
                if (buildResource.isEmpty()) {
                    buildResources.remove(i);
                }
                return true;
            }
        }
        return false;
    }

    public void tryStart() {
        if (structureName != null && StructureTemplateManager.getTemplate(structureName) != null) {
            this.isStarted = true;
        }
    }

    private boolean tryFinish() {
        if (outputSlot.getStackInSlot(0).isEmpty()) {
            ItemStack item = new ItemStack(AWStructureBlocks.STRUCTURE_BUILDER_TICKED);
            item.getOrCreateTag().put(STRUCTURE_NAME_TAG, StringTag.valueOf(structureName));
            outputSlot.setStackInSlot(0, item);
            return true;
        }
        return false;
    }

    public String getCurrentTemplateName() {
        return structureName;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public List<ItemStack> getNeededResources() {
        return buildResources.stream().map(StructureTemplate.BuildResource::getStackRequired).collect(Collectors.toList());
    }

    public void stopCurrentWork() {
        this.structureName = null;
        this.buildResources.clear();
        this.remainingTime = 0;
        this.isFinished = false;
        this.isStarted = false;
        markDirty();
    }

    public void setTemplate(String templateName) {
        if (isStarted) {
            return;
        }
        this.structureName = null;
        this.buildResources.clear();
        this.remainingTime = 0;
        StructureTemplateManager.getTemplate(templateName).ifPresent(t -> {
            if (t.getValidationSettings().isSurvival()) {
                this.structureName = templateName;
            }
            t.getResourceList().forEach(buildResource -> buildResources.add(buildResource.copy()));
            calcTime();
        });
        markDirty();
    }

    private void calcTime() {
        int count = 0;
        for (StructureTemplate.BuildResource resource : this.buildResources) {
            count += resource.getStackRequired().getCount();
        }
        this.totalTime = this.remainingTime = count;
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        inputSlots.deserializeNBT(tag.getCompound("inputInventory"));
        outputSlot.deserializeNBT(tag.getCompound("outputInventory"));
        if (tag.contains(STRUCTURE_NAME_TAG)) {
            structureName = tag.getString(STRUCTURE_NAME_TAG);
        } else {
            structureName = null;
        }
        isStarted = tag.getBoolean("isStarted");
        isFinished = tag.getBoolean("isFinished");
        remainingTime = tag.getInt("remainingTime");
        totalTime = tag.getInt("totalTime");
        buildResources = NBTHelper.deserializeListFrom(tag, "buildResources", StructureTemplate.BuildResource::new);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.put("inputInventory", inputSlots.serializeNBT());

        tag.put("outputInventory", outputSlot.serializeNBT());

        if (structureName != null) {
            tag.putString(STRUCTURE_NAME_TAG, structureName);
        }
        tag.putBoolean("isStarted", isStarted);
        tag.putBoolean("isFinished", isFinished);
        tag.putInt("remainingTime", remainingTime);
        tag.putInt("totalTime", totalTime);
        NBTHelper.writeSerializablesTo(tag, "buildResources", buildResources);

        return tag;
    }

    @Override
    public void onBlockBroken(BlockState state) {
        InventoryTools.dropItemsInWorld(world, inputSlots, pos);
        InventoryTools.dropItemsInWorld(world, outputSlot, pos);
    }
}
