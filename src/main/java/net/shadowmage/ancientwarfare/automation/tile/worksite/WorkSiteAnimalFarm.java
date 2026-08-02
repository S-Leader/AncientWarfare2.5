package net.shadowmage.ancientwarfare.automation.tile.worksite;


import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.ItemStackHandler;
import net.shadowmage.ancientwarfare.automation.config.AWAutomationStatics;
import net.shadowmage.ancientwarfare.core.block.BlockRotationHandler.RelativeSide;
import net.shadowmage.ancientwarfare.core.entity.AWFakePlayer;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.util.EntityTools;
import net.shadowmage.ancientwarfare.core.util.InventoryTools;
import net.shadowmage.ancientwarfare.core.util.ItemWrapper;

import javax.annotation.Nullable;
import java.util.*;

public class WorkSiteAnimalFarm extends TileWorksiteBoundedInventory {
    private static final int FOOD_INVENTORY_SIZE = 3;
    private static final int TOOL_INVENTORY_SIZE = 3;
    private int workerRescanDelay;
    private boolean shouldCountResources;

    public int maxPigCount = 6;
    public int maxCowCount = 6;
    public int maxChickenCount = 6;
    public int maxSheepCount = 6;

    private int wheatCount;
    private int bucketCount;
    private int carrotCount;
    private int potatoCount;
    private int beetrootCount;
    private int seedCount;
    private int shearsSlot = -1;

    private List<EntityPair> pigsToBreed = new ArrayList<>();
    private List<EntityPair> chickensToBreed = new ArrayList<>();
    private List<EntityPair> cowsToBreed = new ArrayList<>();
    private int cowsToMilk;
    private List<EntityPair> sheepToBreed = new ArrayList<>();
    private List<Integer> sheepToShear = new ArrayList<>();
    private List<Integer> entitiesToCull = new ArrayList<>();

    private static final ArrayList<ItemWrapper> ANIMAL_DROPS = ItemWrapper.buildList("Animal Farm drops", AWAutomationStatics.animal_farm_pickups);

    public final ItemStackHandler foodInventory;
    public final ItemStackHandler toolInventory;

    private static final Set<Integer> entityCulledIds = new HashSet<>();
    private static final List<ItemEntity> capturedCullDrops = new ArrayList<>();

    public WorkSiteAnimalFarm() {
        super();
        shouldCountResources = true;

        foodInventory = new ItemStackHandler(FOOD_INVENTORY_SIZE) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
                shouldCountResources = true;
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return isFood(stack.getItem()) ? super.insertItem(slot, stack, simulate) : stack;
            }
        };

        toolInventory = new ItemStackHandler(TOOL_INVENTORY_SIZE) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
                shouldCountResources = true;
            }

            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return isTool(stack.getItem()) ? super.insertItem(slot, stack, simulate) : stack;
            }
        };

        setSideInventory(RelativeSide.FRONT, foodInventory, RelativeSide.FRONT);
        setSideInventory(RelativeSide.BOTTOM, toolInventory, RelativeSide.TOP);
    }

    private boolean isFood(Item item) {
        return item == Items.WHEAT_SEEDS || item == Items.WHEAT || item == Items.CARROT || item == Items.POTATO || item == Items.BEETROOT;
    }

    private boolean isTool(Item item) {
        return item == Items.BUCKET || item instanceof ShearsItem;
    }

    @Override
    public boolean userAdjustableBlocks() {
        return false;
    }

    private boolean canShearSheep() {
        return shearsSlot >= 0 && !sheepToShear.isEmpty();
    }

    private boolean canMilkCow() {
        return bucketCount > 0 && cowsToMilk > 0;
    }

    private boolean canBreedSheep() {
        return wheatCount > 1 && !sheepToBreed.isEmpty();
    }

    private boolean canBreedCows() {
        return wheatCount > 1 && !cowsToBreed.isEmpty();
    }

    private boolean canBreedChicken() {
        return seedCount > 1 && !chickensToBreed.isEmpty();
    }

    private boolean canBreedPigs() {
        return (carrotCount > 1 && !pigsToBreed.isEmpty()) || (potatoCount > 1 && !pigsToBreed.isEmpty()) || (beetrootCount > 1 && !pigsToBreed.isEmpty());
    }

    private boolean canCull() {
        return !entitiesToCull.isEmpty();
    }

    private static final IWorksiteAction SHEAR_ACTION = WorksiteImplementation::getEnergyPerActivation;
    private static final IWorksiteAction MILK_COW_ACTION = WorksiteImplementation::getEnergyPerActivation;
    private static final IWorksiteAction BREED_SHEEP_ACTION = WorksiteImplementation::getEnergyPerActivation;
    private static final IWorksiteAction BREED_COWS_ACTION = WorksiteImplementation::getEnergyPerActivation;
    private static final IWorksiteAction BREED_CHICKEN_ACTION = WorksiteImplementation::getEnergyPerActivation;
    private static final IWorksiteAction BREED_PIGS_ACTION = WorksiteImplementation::getEnergyPerActivation;
    private static final IWorksiteAction CULL_ACTION = WorksiteImplementation::getEnergyPerActivation;

    @Override
    protected Optional<IWorksiteAction> getNextAction() {
        if (canShearSheep()) {
            return Optional.of(SHEAR_ACTION);
        } else if (canMilkCow()) {
            return Optional.of(MILK_COW_ACTION);
        } else if (canBreedSheep()) {
            return Optional.of(BREED_SHEEP_ACTION);
        } else if (canBreedCows()) {
            return Optional.of(BREED_COWS_ACTION);
        } else if (canBreedChicken()) {
            return Optional.of(BREED_CHICKEN_ACTION);
        } else if (canBreedPigs()) {
            return Optional.of(BREED_PIGS_ACTION);
        } else if (canCull()) {
            return Optional.of(CULL_ACTION);
        }
        return Optional.empty();
    }

    @Override
    protected boolean processAction(IWorksiteAction action) {
        if (action == SHEAR_ACTION) {
            return tryShearing();
        } else if (action == MILK_COW_ACTION) {
            if (tryMilking()) {
                InventoryTools.removeItems(toolInventory, new ItemStack(Items.BUCKET), 1);
                InventoryTools.insertOrDropItem(mainInventory, new ItemStack(Items.MILK_BUCKET), world, pos);
                return true;
            }
        } else if (action == BREED_SHEEP_ACTION) {
            if (tryBreeding(sheepToBreed)) {
                wheatCount -= 2;
                InventoryTools.removeItems(foodInventory, new ItemStack(Items.WHEAT), 2);
                return true;
            }
        } else if (action == BREED_COWS_ACTION) {
            if (tryBreeding(cowsToBreed)) {
                wheatCount -= 2;
                InventoryTools.removeItems(foodInventory, new ItemStack(Items.WHEAT), 2);
                return true;
            }
        } else if (action == BREED_CHICKEN_ACTION) {
            if (tryBreeding(chickensToBreed)) {
                seedCount -= 2;
                InventoryTools.removeItems(foodInventory, new ItemStack(Items.WHEAT_SEEDS), 2);
                return true;
            }
        } else if (action == BREED_PIGS_ACTION) {
            if (tryBreeding(pigsToBreed)) {
                if (carrotCount > 1) {
                    carrotCount -= 2;
                    InventoryTools.removeItems(foodInventory, new ItemStack(Items.CARROT), 2);
                    return true;
                } else if (potatoCount > 1) {
                    potatoCount -= 2;
                    InventoryTools.removeItems(foodInventory, new ItemStack(Items.POTATO), 2);
                    return true;
                } else if (beetrootCount > 1) {
                    beetrootCount -= 2;
                    InventoryTools.removeItems(foodInventory, new ItemStack(Items.BEETROOT), 2);
                    return true;
                }
            }
        } else if (action == CULL_ACTION) {
            return tryCulling();
        }
        return false;
    }

    @Override
    protected void updateWorksite() {
        world.getProfiler().push("Count Resources");
        if (shouldCountResources) {
            countResources();
            this.shouldCountResources = false;
        }
        world.getProfiler().popPush("Animal Rescan");
        if (workerRescanDelay-- <= 0) {
            rescan();
            workerRescanDelay = 200;
        }
        world.getProfiler().popPush("ItemPickup");
        if (world.getDayTime() % 128 == 0) {
            pickupDrops();
        }
        world.getProfiler().pop();
    }

    @Override
    public void onBlockBroken(BlockState state) {
        super.onBlockBroken(state);
        InventoryTools.dropItemsInWorld(world, foodInventory, pos);
        InventoryTools.dropItemsInWorld(world, toolInventory, pos);
    }

    private void countResources() {
        carrotCount = InventoryTools.getCountOf(foodInventory, s -> s.getItem() == Items.CARROT);
        potatoCount = InventoryTools.getCountOf(foodInventory, s -> s.getItem() == Items.POTATO);
        beetrootCount = InventoryTools.getCountOf(foodInventory, s -> s.getItem() == Items.BEETROOT);
        seedCount = InventoryTools.getCountOf(foodInventory, s -> s.getItem() == Items.WHEAT_SEEDS);
        wheatCount = InventoryTools.getCountOf(foodInventory, s -> s.getItem() == Items.WHEAT);
        bucketCount = InventoryTools.getCountOf(toolInventory, s -> s.getItem() == Items.BUCKET);
        shearsSlot = InventoryTools.findItemSlot(toolInventory, s -> s.getItem() instanceof ShearsItem);
    }

    private void rescan() {
        pigsToBreed.clear();
        cowsToBreed.clear();
        cowsToMilk = 0;
        sheepToBreed.clear();
        chickensToBreed.clear();
        entitiesToCull.clear();

        List<Animal> entityList = EntityTools.getEntitiesWithinBounds(world, Animal.class, getWorkBoundsMin(), getWorkBoundsMax());

        List<Animal> cows = new ArrayList<>();
        List<Animal> pigs = new ArrayList<>();
        List<Animal> sheep = new ArrayList<>();
        List<Animal> chickens = new ArrayList<>();

        for (Animal animal : entityList) {
            if (animal instanceof Cow) {
                cows.add(animal);
            } else if (animal instanceof Chicken) {
                chickens.add(animal);
            } else if (animal instanceof Sheep) {
                sheep.add(animal);
            } else if (animal instanceof Pig) {
                pigs.add(animal);
            }
        }

        scanForCows(cows);
        scanForSheep(sheep);
        scanForAnimals(chickens, chickensToBreed, maxChickenCount);
        scanForAnimals(pigs, pigsToBreed, maxPigCount);
    }

    private void scanForAnimals(List<Animal> animals, List<EntityPair> targets, int maxCount) {
        Animal animal1;
        Animal animal2;
        EntityPair breedingPair;

        int age;

        for (int i = 0; i < animals.size(); i++) {
            animal1 = animals.get(i);
            age = animal1.getAge();
            if (age != 0 || animal1.isInLove()) {
                continue;
            }//unbreedable first-target, skip
            while (i + 1 < animals.size())//loop through remaining animals to find a breeding partner
            {
                i++;
                animal2 = animals.get(i);
                age = animal2.getAge();
                if (age == 0 && !animal2.isInLove())//found a second breedable animal, add breeding pair, exit to outer loop
                {
                    breedingPair = new EntityPair(animal1, animal2);
                    targets.add(breedingPair);
                    break;
                }
            }
        }

        int grownCount = 0;
        for (Animal animal : animals) {
            if (animal.getAge() >= 0) {
                grownCount++;
            }
        }

        if (grownCount > maxCount) {
            for (int i = 0, cullCount = grownCount - maxCount; i < animals.size() && cullCount > 0; i++) {
                if (animals.get(i).getAge() >= 0) {
                    entitiesToCull.add(animals.get(i).getId());
                    cullCount--;
                }
            }
        }
    }

    private void scanForSheep(List<Animal> sheep) {
        scanForAnimals(sheep, sheepToBreed, maxSheepCount);
        for (Animal animal : sheep) {
            if (animal.getAge() >= 0) {
                Sheep sheep1 = (Sheep) animal;
                if (!sheep1.isSheared()) {
                    sheepToShear.add(sheep1.getId());
                }
            }
        }
    }

    private void scanForCows(List<Animal> animals) {
        scanForAnimals(animals, cowsToBreed, maxCowCount);
        for (Animal animal : animals) {
            if (animal.getAge() >= 0) {
                cowsToMilk++;
            }
        }
    }

    private boolean tryBreeding(List<EntityPair> targets) {
        Entity animalA;
        Entity animalB;
        EntityPair pair;
        if (!targets.isEmpty()) {
            pair = targets.remove(0);
            animalA = pair.getEntityA(world);
            animalB = pair.getEntityB(world);
            if (!(animalA instanceof Animal) || !(animalB instanceof Animal)) {
                return false;
            }
            if (animalA.isAlive() && animalB.isAlive()) {
                Player fakePlayer = AWFakePlayer.get(world);
                ((Animal) animalA).setInLove(fakePlayer);
                ((Animal) animalB).setInLove(fakePlayer);
                return true;
            }
        }
        return false;
    }

    private boolean tryMilking() {
        return cowsToMilk > 0 && world.getRandom().nextInt(cowsToMilk * 4) > (cowsToMilk * 3);
    }

    private boolean tryShearing() {
        if (shearsSlot < 0 || sheepToShear.isEmpty()) {
            return false;
        }
        Sheep sheep = (Sheep) world.getEntity(sheepToShear.remove(0));
        ItemStack shears = toolInventory.getStackInSlot(shearsSlot);
        if (sheep == null || !sheep.isShearable(shears, world, pos)) {
            return false;
        }
        //shears do not get damaged, if they did this would need clone of the stack and additional setStackInSlot call
        NonNullList<ItemStack> items = InventoryTools.toNonNullList(sheep.onSheared(null, shears, world, pos, getFortune()));
        for (ItemStack item : items) {
            InventoryTools.insertOrDropItem(mainInventory, item, world, pos);
        }
        return true;
    }

    private boolean tryCulling() {
        Entity entity;
        Animal animal;
        int fortune = getFortune();
        while (canCull()) {
            entity = world.getEntity(entitiesToCull.remove(0));
            if (entity instanceof Animal && entity.isAlive()) {
                animal = (Animal) entity;
                if (animal.isInLove() || animal.getAge() < 0) {
                    continue;
                }

                entityCulledIds.add(animal.getId());
                capturedCullDrops.clear();
                animal.hurt(world.damageSources().generic(), animal.getHealth() + 1);
                for (ItemEntity item : capturedCullDrops) {
                    ItemStack stack = item.getItem();
                    if (!stack.isEmpty()) {
                        if (fortune > 0) {
                            stack.grow(world.getRandom().nextInt(fortune));
                        }
                        InventoryTools.insertOrDropItem(mainInventory, stack, world, pos);
                    }
                }
                capturedCullDrops.clear();
                entityCulledIds.remove(animal.getId());
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent evt) {
        if (entityCulledIds.contains(evt.getEntity().getId())) {
            //capture drops for the culling worksite to collect - cancelling the event prevents them spawning in the world
            capturedCullDrops.addAll(evt.getDrops());
            evt.setCanceled(true);
        }
    }

    @Override
    public boolean onBlockClicked(Player player, @Nullable InteractionHand hand) {
        if (!player.level().isClientSide) {
            AWMenuTypes.open(player, NetworkHandler.GUI_WORKSITE_ANIMAL_FARM, pos);
        }
        return true;
    }

    private void pickupDrops() {
        List<ItemEntity> items = EntityTools.getEntitiesWithinBounds(world, ItemEntity.class, getWorkBoundsMin(), getWorkBoundsMax());
        for (ItemEntity item : items) {
            ItemStack stack = item.getItem();
            if (item.isAlive() && !stack.isEmpty() && stack.getItem() != Items.AIR) {
                Item droppedItem = stack.getItem();
                for (ItemWrapper animalDrop : ANIMAL_DROPS) {
                    if (droppedItem.equals(animalDrop.item) && animalDrop.damage == -1 || animalDrop.damage == stack.getDamageValue()) {
                        stack = InventoryTools.mergeItemStack(mainInventory, stack);
                        if (!stack.isEmpty()) {
                            item.setItem(stack);
                        } else {
                            item.discard();
                        }
                    }
                }
            }
        }
    }

    @Override
    public WorkType getWorkType() {
        return WorkType.FARMING;
    }

    @Override
    public void openAltGui(Player player) {
        AWMenuTypes.open(player, NetworkHandler.GUI_WORKSITE_ANIMAL_CONTROL, pos);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        maxChickenCount = tag.getInt("maxChickens");
        maxCowCount = tag.getInt("maxCows");
        maxPigCount = tag.getInt("maxPigs");
        maxSheepCount = tag.getInt("maxSheep");
        foodInventory.deserializeNBT(tag.getCompound("foodInventory"));
        toolInventory.deserializeNBT(tag.getCompound("toolInventory"));
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        tag.putInt("maxChickens", maxChickenCount);
        tag.putInt("maxCows", maxCowCount);
        tag.putInt("maxPigs", maxPigCount);
        tag.putInt("maxSheep", maxSheepCount);
        tag.put("foodInventory", foodInventory.serializeNBT());
        tag.put("toolInventory", toolInventory.serializeNBT());
        return tag;
    }

    private static class EntityPair {

        private final int idA;
        private final int idB;

        private EntityPair(Entity a, Entity b) {
            idA = a.getId();
            idB = b.getId();
        }

        private Entity getEntityA(Level world) {
            return world.getEntity(idA);
        }

        private Entity getEntityB(Level world) {
            return world.getEntity(idB);
        }
    }

}
