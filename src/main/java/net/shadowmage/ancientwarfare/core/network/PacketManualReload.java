package net.shadowmage.ancientwarfare.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.manual.ManualContentRegistry;
import net.shadowmage.ancientwarfare.core.registry.RegistryLoader;

import java.io.IOException;

public class PacketManualReload extends PacketBase {
    @Override
    protected void writeToStream(ByteBuf data) {
        //noop
    }

    @Override
    protected void readFromStream(ByteBuf data) throws IOException {
        //noop
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void execute() {
        ManualContentRegistry.clearContents();
        RegistryLoader.reload("manual_content");
        Minecraft.getInstance().player.sendSystemMessage(Component.literal("Manual content reloaded"));
    }
}
