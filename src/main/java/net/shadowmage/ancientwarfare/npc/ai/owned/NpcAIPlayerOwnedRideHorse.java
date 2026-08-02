package net.shadowmage.ancientwarfare.npc.ai.owned;

import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.npc.ai.NpcAIRideHorse;
import net.shadowmage.ancientwarfare.npc.entity.NpcPlayerOwned;

public class NpcAIPlayerOwnedRideHorse extends NpcAIRideHorse<NpcPlayerOwned> {
    private boolean saddled = false;

    public NpcAIPlayerOwnedRideHorse(NpcPlayerOwned npc) {
        super(npc, 1.0);
    }

    @Override
    public void tick() {
        if (horse != npc.getVehicle() && horse != null) {
            onDismountHorse();
            horse = null;
        }
    }

    @Override
    public void stop() {
        if (horse != null) {
            onDismountHorse();
        }
        horse = null;
    }

    @Override
    protected void onMountHorse() {
        if (horse instanceof AbstractHorse) {
            saddled = ((AbstractHorse) horse).isSaddled();
        }
        super.onMountHorse();
    }

    @Override
    protected void onDismountHorse() {
        super.onDismountHorse();
        if (horse instanceof AbstractHorse && !saddled) {
            //restore original unsaddled state (super re-saddles on dismount)
            ((AbstractHorse) horse).getSlot(AbstractHorse.INVENTORY_SLOT_OFFSET).set(ItemStack.EMPTY);
        }
    }
}
