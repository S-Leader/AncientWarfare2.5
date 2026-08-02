package net.shadowmage.ancientwarfare.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.shadowmage.ancientwarfare.core.input.IScrollableItem;

public class PacketItemMouseScroll extends PacketBase {
    private boolean scrollUp;

    public PacketItemMouseScroll() {
    }

    public PacketItemMouseScroll(boolean scrollUp) {
        this.scrollUp = scrollUp;
    }

    @Override
    protected void writeToStream(ByteBuf data) {
        data.writeBoolean(scrollUp);
    }

    @Override
    protected void readFromStream(ByteBuf data) {
        scrollUp = data.readBoolean();
    }

    @Override
    protected void execute(Player player) {
        ItemStack stack = player.getMainHandItem();
        Item item = stack.getItem();
        if (item instanceof IScrollableItem) {
            if (scrollUp) {
                ((IScrollableItem) item).onScrollUp(player.level(), player, stack);
            } else {
                ((IScrollableItem) item).onScrollDown(player.level(), player, stack);
            }
        }
    }
}
