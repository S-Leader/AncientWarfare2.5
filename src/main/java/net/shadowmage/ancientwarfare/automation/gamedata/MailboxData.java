package net.shadowmage.ancientwarfare.automation.gamedata;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.shadowmage.ancientwarfare.automation.tile.TileMailbox;
import net.shadowmage.ancientwarfare.core.compat.WorldSavedData;
import net.shadowmage.ancientwarfare.core.util.Constants;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.Trig;

import javax.annotation.Nullable;
import java.util.*;

import static net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics.mailboxTimeForDimension;
import static net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics.mailboxTimePerBlock;

public class MailboxData extends WorldSavedData {
    private MailboxSet publicMailboxes = new MailboxSet("public");
    private HashMap<String, MailboxSet> privateMailboxes = new HashMap<>();

    public MailboxData(String par1Str) {
        super(par1Str);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent evt) {
        if (evt.phase == TickEvent.Phase.END) {
            onTick(1);
        }
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        publicMailboxes = new MailboxSet("public");
        publicMailboxes.readFromNBT(tag.getCompound("publicBoxes"));

        ListTag privateBoxList = tag.getList("privateBoxes", Constants.NBT.TAG_COMPOUND);
        privateMailboxes.clear();
        MailboxSet boxSet;
        for (int i = 0; i < privateBoxList.size(); i++) {
            boxSet = new MailboxSet();
            boxSet.readFromNBT(privateBoxList.getCompound(i));
            privateMailboxes.put(boxSet.owningPlayerName, boxSet);
        }
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        tag.put("publicBoxes", publicMailboxes.writeToNBT(new CompoundTag()));

        ListTag privateBoxList = new ListTag();
        for (MailboxSet set : this.privateMailboxes.values()) {
            privateBoxList.add(set.writeToNBT(new CompoundTag()));
        }
        tag.put("privateBoxes", privateBoxList);
        return tag;
    }

    public void onTick(int length) {
        synchronized (publicMailboxes) {
            boolean change = publicMailboxes.tick(length);
            if (change) {
                markDirty();
            }
        }
        synchronized (privateMailboxes) {
            boolean change = false;
            for (MailboxSet set : this.privateMailboxes.values()) {
                change |= set.tick(length);
            }
            if (change) {
                markDirty();
            }
        }
    }

    public boolean addMailbox(@Nullable String owner, String name) {
        MailboxSet set = owner == null ? publicMailboxes : getOrCreatePrivateMailbox(owner);
        markDirty();
        return set.addMailbox(name);
    }

    public boolean deleteMailbox(@Nullable String owner, String name) {
        MailboxSet set = owner == null ? publicMailboxes : getOrCreatePrivateMailbox(owner);
        markDirty();
        return set.deleteMailbox(name);
    }

    public void addDeliverableItem(@Nullable String owner, String name, ItemStack item, int dim, BlockPos pos) {
        MailboxSet set = owner == null ? publicMailboxes : getOrCreatePrivateMailbox(owner);
        MailboxEntry entry = set.getOrCreateMailbox(name);
        entry.addDeliverableItem(item, dim, pos);
        markDirty();
    }

    private MailboxSet getOrCreatePrivateMailbox(@Nullable String owner) {
        if (owner == null) {
            owner = "";
        }
        if (!privateMailboxes.containsKey(owner)) {
            privateMailboxes.put(owner, new MailboxSet(owner));
            markDirty();
        }
        return privateMailboxes.get(owner);
    }

    public List<String> getPublicBoxNames() {
        ArrayList<String> names = new ArrayList<>();
        MailboxSet set = publicMailboxes;
        names.addAll(set.mailboxes.keySet());
        return names;
    }

    public List<String> getPrivateBoxNames(String owner) {
        ArrayList<String> names = new ArrayList<>();
        if (privateMailboxes.containsKey(owner)) {
            names.addAll(privateMailboxes.get(owner).mailboxes.keySet());
        }
        return names;
    }

    public List<DeliverableItem> getDeliverableItems(@Nullable String owner, String name, List<DeliverableItem> items, Level world, int x, int y, int z) {
        MailboxSet set = owner == null ? publicMailboxes : getOrCreatePrivateMailbox(owner);
        return set.getDeliverableItems(name, items, world, x, y, z);
    }

    public void removeDeliverableItem(@Nullable String owner, String name, DeliverableItem item) {
        MailboxSet set = owner == null ? publicMailboxes : getOrCreatePrivateMailbox(owner);
        set.removeDeliverableItem(name, item);
        markDirty();
    }

    public void addMailboxReceiver(@Nullable String owner, String name, TileMailbox box) {
        MailboxSet set = owner == null ? publicMailboxes : getOrCreatePrivateMailbox(owner);
        set.addReceiver(name, box);
        markDirty();
    }

    private static int getLegacyDimensionId(Level world) {
        if (world.dimension() == Level.NETHER) {
            return -1;
        }
        if (world.dimension() == Level.END) {
            return 1;
        }
        return 0;
    }

    private final class MailboxSet {

        private String owningPlayerName;
        private HashMap<String, MailboxEntry> mailboxes = new HashMap<>();

        private MailboxSet(String name) {
            this.owningPlayerName = name;
        }

        private MailboxSet() {
        }//nbt constructor

        private boolean addMailbox(String name) {
            if (mailboxes.containsKey(name)) {
                return false;
            }
            mailboxes.put(name, new MailboxEntry(name));
            return true;
        }

        private boolean deleteMailbox(String name) {
            if (!mailboxes.containsKey(name)) {
                return false;
            }
            if (!mailboxes.get(name).incomingItems.isEmpty()) {
                return false;
            }
            mailboxes.remove(name);
            return true;
        }

        private void readFromNBT(CompoundTag tag) {
            ListTag mailboxList = tag.getList("mailboxList", Constants.NBT.TAG_COMPOUND);
            CompoundTag mailboxTag;
            mailboxes.clear();
            MailboxEntry entry;
            for (int i = 0; i < mailboxList.size(); i++) {
                mailboxTag = mailboxList.getCompound(i);
                entry = new MailboxEntry();
                entry.readFromNBT(mailboxTag);
                mailboxes.put(entry.mailboxName, entry);
            }
            owningPlayerName = tag.getString("ownerName");
        }

        private CompoundTag writeToNBT(CompoundTag tag) {
            ListTag mailboxList = new ListTag();
            CompoundTag mailboxTag;
            for (MailboxEntry entry : this.mailboxes.values()) {
                mailboxTag = entry.writeToNBT(new CompoundTag());
                mailboxList.add(mailboxTag);
            }
            tag.put("mailboxList", mailboxList);
            tag.putString("ownerName", owningPlayerName);
            return tag;
        }

        private boolean tick(int length) {
            boolean ret = false;
            for (MailboxEntry entry : this.mailboxes.values()) {
                ret |= entry.tick(length);
            }
            return ret;
        }

        private MailboxEntry getOrCreateMailbox(String name) {
            if (!this.mailboxes.containsKey(name)) {
                this.mailboxes.put(name, new MailboxEntry(name));
            }
            return this.mailboxes.get(name);
        }

        private List<DeliverableItem> getDeliverableItems(String name, List<DeliverableItem> items, Level world, int x, int y, int z) {
            if (this.mailboxes.containsKey(name)) {
                return this.mailboxes.get(name).getDeliverableItems(items, world, x, y, z);
            }
            return Collections.emptyList();
        }

        private void removeDeliverableItem(String name, DeliverableItem item) {
            if (this.mailboxes.containsKey(name)) {
                this.mailboxes.get(name).removeDeliverableItem(item);
            }
        }

        private void addReceiver(String name, TileMailbox box) {
            if (this.mailboxes.containsKey(name)) {
                this.mailboxes.get(name).addReceiver(box);
            }
        }
    }

    public final class MailboxEntry {
        private String mailboxName;
        private List<DeliverableItem> incomingItems = new ArrayList<>();
        private List<TileMailbox> receivers = new ArrayList<>();

        private MailboxEntry(String name) {
            this.mailboxName = name;
        }

        private MailboxEntry() {
        }//nbt-constructor

        private void addReceiver(TileMailbox tile) {
            this.receivers.add(tile);
        }

        private void removeDeliverableItem(DeliverableItem item) {
            incomingItems.remove(item);
        }

        private List<DeliverableItem> getDeliverableItems(List<DeliverableItem> items, Level world, int x, int y, int z) {
            int dim = getLegacyDimensionId(world);
            int time = 0;
            int timePerBlock = mailboxTimePerBlock;//set time from config for per-block time
            int timeForDimension = mailboxTimeForDimension;//set time from config for cross-dimensional items
            for (DeliverableItem item : this.incomingItems) {
                if (dim != item.originDimension) {
                    time = timeForDimension;
                } else {
                    float dist = Trig.getDistance(item.x, item.y, item.z, x, y, z);
                    time = (int) (dist * (float) timePerBlock);
                }
                if (item.deliveryTime >= time) {
                    items.add(item);
                }
            }
            return items;
        }

        private void addDeliverableItem(ItemStack item, int dimension, BlockPos pos) {
            DeliverableItem item1 = new DeliverableItem(item, dimension, pos.getX(), pos.getY(), pos.getZ());
            incomingItems.add(item1);
        }

        private void readFromNBT(CompoundTag tag) {
            mailboxName = tag.getString("name");
            ListTag itemList = tag.getList("itemList", Constants.NBT.TAG_COMPOUND);
            CompoundTag itemTag;
            DeliverableItem item;
            for (int i = 0; i < itemList.size(); i++) {
                itemTag = itemList.getCompound(i);
                item = new DeliverableItem();
                item.readFromNBT(itemTag);
                incomingItems.add(item);
            }
        }

        private CompoundTag writeToNBT(CompoundTag tag) {
            tag.putString("name", mailboxName);
            ListTag itemList = new ListTag();
            CompoundTag itemTag;
            for (DeliverableItem item : this.incomingItems) {
                itemTag = item.writeToNBT(new CompoundTag());
                itemList.add(itemTag);
            }
            tag.put("itemList", itemList);
            return tag;
        }

        private boolean tick(int length) {
            boolean ret = incomingItems.size() > 0;
            for (DeliverableItem item : this.incomingItems) {
                item.tick(length);
            }

            int dim;
            int time = 0;
            int timePerBlock = mailboxTimePerBlock;//set time from config for per-block time
            int timeForDimension = mailboxTimeForDimension;//set time from config for cross-dimensional items
            int x, y, z;
            Iterator<DeliverableItem> it;
            DeliverableItem item;
            ItemStack stack;
            for (TileMailbox box : receivers) {
                dim = getLegacyDimensionId(box.getWorld());
                x = box.getPos().getX();
                y = box.getPos().getY();
                z = box.getPos().getZ();
                it = this.incomingItems.iterator();
                while (it.hasNext() && (item = it.next()) != null) {
                    if (dim != item.originDimension) {
                        time = timeForDimension;
                    } else {
                        float dist = Trig.getDistance(item.x, item.y, item.z, x, y, z);
                        time = (int) (dist * (float) timePerBlock);
                    }
                    if (item.deliveryTime >= time)//find if item is deliverable to this box
                    {
                        stack = InventoryTools.mergeItemStack(box.receivedInventory, item.item);
                        if (stack.isEmpty()) {
                            it.remove();
                        }
                        break;
                    }
                }
            }
            receivers.clear();

            return ret;
        }

        @Override
        public String toString() {
            return "MailboxEntry: " + getName() + " Items List: " + incomingItems;
        }
    }

    public final class DeliverableItem {
        int originDimension;
        int x, y, z;
        public ItemStack item;
        int deliveryTime;//system milis at which this stack is deliverable

        private DeliverableItem(ItemStack item, int dim, int x, int y, int z) {
            this.item = item;
            this.originDimension = dim;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private DeliverableItem() {
        }

        private void readFromNBT(CompoundTag tag) {
            item = ItemStack.of(tag.getCompound("item"));
            deliveryTime = tag.getInt("time");
            originDimension = tag.getInt("dim");
            this.x = tag.getInt("x");
            this.y = tag.getInt("y");
            this.z = tag.getInt("z");
        }

        private CompoundTag writeToNBT(CompoundTag tag) {
            tag.put("item", item.save(new CompoundTag()));
            tag.putInt("time", deliveryTime);
            tag.putInt("dim", originDimension);
            tag.putInt("x", x);
            tag.putInt("y", y);
            tag.putInt("z", z);
            return tag;
        }

        private void tick(int length) {
            deliveryTime += length;
        }
    }

}
