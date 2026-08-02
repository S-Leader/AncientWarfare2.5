package net.shadowmage.ancientwarfare.core.input;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.IKeyConflictContext;

@OnlyIn(Dist.CLIENT)
public class ItemKeyConflictContext implements IKeyConflictContext {
    public static final ItemKeyConflictContext INSTANCE = new ItemKeyConflictContext();

    private ItemKeyConflictContext() {
    }

    @Override
    public boolean isActive() {
        Minecraft mc = Minecraft.getInstance();
        //noinspection SimplifiableIfStatement
        if (mc.screen != null || mc.player == null || mc.level == null) {
            return false;
        }

        return mc.player.getMainHandItem().getItem() instanceof IItemKeyInterface || mc.player.getOffhandItem().getItem() instanceof IItemKeyInterface;
    }

    @Override
    public boolean conflicts(IKeyConflictContext other) {
        return this == other;
    }
}
