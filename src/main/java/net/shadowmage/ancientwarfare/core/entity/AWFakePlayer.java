package net.shadowmage.ancientwarfare.core.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;

import java.util.UUID;

public class AWFakePlayer extends FakePlayer {
    private static final String PLAYER_NAME = "AncientWarfareFakePlayer";
    private static AWFakePlayer instance;

    private AWFakePlayer(ServerLevel world) {
        super(world, new GameProfile(UUID.nameUUIDFromBytes(PLAYER_NAME.getBytes()), PLAYER_NAME));
    }

    public static AWFakePlayer get(Level world) {
        if (instance == null && world instanceof ServerLevel) {
            instance = new AWFakePlayer((ServerLevel) world);
        }
        return instance;
    }

    @Override
    protected void onInsideBlock(BlockState p_191955_1_) {
        //noop
    }

    public static void onWorldUnload() {
        instance = null;
    }
}
