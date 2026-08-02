package net.shadowmage.ancientwarfare.npc.proxy;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.shadowmage.ancientwarfare.core.proxy.CommonProxyBase;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Optional;

public class NpcCommonProxy extends CommonProxyBase {
    private HashSet<GameProfile> profileCache = new HashSet<>();

    public void loadSkins() {
        //overriden in client proxy
    }

    @SuppressWarnings("squid:S1172")
    public Optional<ResourceLocation> loadSkinPackImage(String imageName, InputStream is) {
        return Optional.empty();
    }

    public Optional<GameProfile> getProfile(final String name) {
        return profileCache.stream().filter(input -> input.getName().equals(name)).findFirst();
    }

    public void cacheProfile(GameProfile gameprofile) {
        profileCache.add(gameprofile);
    }

    public Optional<CompoundTag> cacheProfile(ServerLevel worldServer, final String name) {
        if (name.isEmpty()) {
            return Optional.empty();
        }

        Optional<GameProfile> gp = getProfile(name);
        if (!gp.isPresent()) {
            //noinspection ConstantConditions
            gp = worldServer.getServer().getProfileCache().get(name);
            if (gp.isPresent()) {
                if (!gp.get().getProperties().get("textures").stream().findFirst().isPresent()) {
                    gp = Optional.of(worldServer.getServer().getSessionService().fillProfileProperties(gp.get(), true));
                }
                gp.ifPresent(this::cacheProfile);
            }
        }
        return gp.map(p -> {
            CompoundTag tagCompound = new CompoundTag();
            NbtUtils.writeGameProfile(tagCompound, p);
            return tagCompound;
        });
    }

    @SuppressWarnings("squid:S1172")
    public Optional<ResourceLocation> getPlayerSkin(String name) {
        return Optional.empty();
    }
}
