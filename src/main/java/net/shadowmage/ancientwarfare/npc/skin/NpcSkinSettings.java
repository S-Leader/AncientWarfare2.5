package net.shadowmage.ancientwarfare.npc.skin;

import com.google.common.collect.ImmutableMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.core.network.PacketEntity;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;
import net.shadowmage.ancientwarfare.npc.entity.NpcBase;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class NpcSkinSettings {
    private static final ResourceLocation BASE_DEFAULT_TEXTURE = new ResourceLocation("ancientwarfare:textures/entity/npc/npc_default.png");
    public static final String PACKET_TAG_NAME = "skinSettings";
    private static final String PLAYER_NAME_TAG = "playerName";
    private static final String RANDOM_TAG = "random";
    private static final String NPC_TYPE_NAME_TAG = "npcTypeName";
    private static final String NPC_TYPE_SKIN_TAG = "npcTypeSkin";
    private static final String IS_ALEX_MODEL_TAG = "isAlexModel";

    private SkinType skinTypeSelected = SkinType.DEFAULT;
    private boolean playerSkinLoaded = false;
    private ResourceLocation playerSkin = null;
    private String playerName = "";
    private boolean random = true;
    private String npcTypeName = "custom";
    private ResourceLocation npcTypeSkin = null;

    public boolean isAlexModel() {
        return isAlexModel;
    }

    public void setAlexModel(boolean alexModel) {
        isAlexModel = alexModel;
    }

    public boolean renderFemaleModel(NpcBase npc) {
        if (skinTypeSelected == SkinType.DEFAULT) {
            return npc.isFemale();
        }
        return isAlexModel;
    }

    private boolean isAlexModel = false;

    public ResourceLocation getTexture(NpcBase npc) {
        switch (skinTypeSelected) {
            case PLAYER:
                loadPlayerSkin();
                return playerSkin == null ? BASE_DEFAULT_TEXTURE : playerSkin;
            case NPC_TYPE:
                return random || npcTypeSkin == null ? NpcSkinManager.getNpcTexture(npcTypeName, npc.getIDForSkin()).orElse(BASE_DEFAULT_TEXTURE) : npcTypeSkin;
            case DEFAULT:
            default:
                return NpcSkinManager.getNpcTexture(npc.getNpcFullType(), npc.getIDForSkin()).orElse(BASE_DEFAULT_TEXTURE);
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("skinTypeSelected", skinTypeSelected.getName());

        if (!playerName.isEmpty()) {
            tag.putString(PLAYER_NAME_TAG, playerName);
        }
        if (!random) {
            tag.putBoolean(RANDOM_TAG, false);
        }
        if (!npcTypeName.isEmpty()) {
            tag.putString(NPC_TYPE_NAME_TAG, npcTypeName);
        }
        if (npcTypeSkin != null) {
            tag.putString(NPC_TYPE_SKIN_TAG, npcTypeSkin.toString());
        }
        if (isAlexModel) {
            tag.putBoolean(IS_ALEX_MODEL_TAG, true);
        }

        return tag;
    }

    public void serializeToBuffer(ByteBuf buffer) {
        FriendlyByteBuf packetBuffer = new FriendlyByteBuf(buffer);
        packetBuffer.writeUtf(skinTypeSelected.getName());
        packetBuffer.writeUtf(playerName);
        packetBuffer.writeBoolean(random);
        packetBuffer.writeUtf(npcTypeName);
        packetBuffer.writeUtf(npcTypeSkin == null ? "" : npcTypeSkin.toString().substring(0, Math.min(npcTypeSkin.toString().length(), 100)));
        packetBuffer.writeBoolean(isAlexModel);
    }

    public static NpcSkinSettings deserializeFromBuffer(ByteBuf buffer) {
        NpcSkinSettings skinSettings = new NpcSkinSettings();
        FriendlyByteBuf packetBuffer = new FriendlyByteBuf(buffer);
        skinSettings.skinTypeSelected = SkinType.byName(packetBuffer.readUtf(20));
        if (skinSettings.skinTypeSelected == SkinType.PLAYER) {
            skinSettings.playerName = packetBuffer.readUtf(40);
        } else {
            packetBuffer.readUtf(40);
        }
        if (skinSettings.skinTypeSelected == SkinType.NPC_TYPE) {
            skinSettings.random = packetBuffer.readBoolean();
            skinSettings.npcTypeName = packetBuffer.readUtf(30);
            String skin = packetBuffer.readUtf(100);
            if (!skinSettings.random) {
                skinSettings.npcTypeSkin = skin.isEmpty() ? null : new ResourceLocation(skin);
            }
        } else {
            packetBuffer.readBoolean();
            packetBuffer.readUtf(30);
            packetBuffer.readUtf(60);
        }
        skinSettings.isAlexModel = packetBuffer.readBoolean();
        return skinSettings;
    }

    public static NpcSkinSettings deserializeNBT(CompoundTag tag) {
        NpcSkinSettings skinSettings = new NpcSkinSettings();

        skinSettings.skinTypeSelected = SkinType.byName(tag.getString("skinTypeSelected"));

        if (tag.contains(PLAYER_NAME_TAG)) {
            skinSettings.playerName = tag.getString(PLAYER_NAME_TAG);
        }
        if (tag.contains(RANDOM_TAG)) {
            skinSettings.random = tag.getBoolean(RANDOM_TAG);
        }
        if (tag.contains(NPC_TYPE_NAME_TAG)) {
            skinSettings.npcTypeName = tag.getString(NPC_TYPE_NAME_TAG);
        }
        if (tag.contains(NPC_TYPE_SKIN_TAG)) {
            skinSettings.npcTypeSkin = new ResourceLocation(tag.getString(NPC_TYPE_SKIN_TAG));
        }
        skinSettings.isAlexModel = tag.getBoolean(IS_ALEX_MODEL_TAG);
        return skinSettings;
    }

    public NpcSkinSettings minimizeData() {
        NpcSkinSettings skinSettings = new NpcSkinSettings();
        skinSettings.skinTypeSelected = skinTypeSelected;
        if (skinTypeSelected == SkinType.PLAYER) {
            skinSettings.playerName = playerName;

        } else if (skinTypeSelected == SkinType.NPC_TYPE) {
            skinSettings.random = random;
            skinSettings.npcTypeName = npcTypeName;
            if (!random) {
                skinSettings.npcTypeSkin = npcTypeSkin;
            }
        }
        if (skinTypeSelected != SkinType.DEFAULT) {
            skinSettings.isAlexModel = isAlexModel;
        }
        return skinSettings;
    }

    private void loadPlayerSkin() {
        if (!playerSkinLoaded) {
            Optional<ResourceLocation> plSkin = AncientWarfareNPC.proxy.getPlayerSkin(playerName);
            if (plSkin.isPresent()) {
                playerSkin = plSkin.get();
                playerSkinLoaded = true;
            }
        }
    }

    private void loadPlayerProfile(NpcBase npc) {
        Level world = npc.level();
        if (!world.isClientSide) {
            sendEntityPacket(npc, data -> AncientWarfareNPC.proxy.cacheProfile((ServerLevel) world, playerName).ifPresent(t -> data.put("profile", t)));
        }
    }

    private void sendEntityPacket(NpcBase npc, Consumer<CompoundTag> setData) {
        PacketEntity pkt = new PacketEntity(npc);
        CompoundTag data = new CompoundTag();
        setData.accept(data);
        pkt.packetData.put(PACKET_TAG_NAME, data);
        NetworkHandler.sendToAllTracking(npc, pkt);
    }

    public void handlePacketData(CompoundTag tag) {
        if (skinTypeSelected == SkinType.PLAYER) {
            Optional.ofNullable(NbtUtils.readGameProfile(tag.getCompound(PACKET_TAG_NAME).getCompound("profile"))).ifPresent(AncientWarfareNPC.proxy::cacheProfile);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public String getDescription() {
        switch (skinTypeSelected) {
            case PLAYER:
                return I18n.get("gui.ancientwarfarenpc.skin_info.player", playerName);
            case NPC_TYPE:
                return random || npcTypeSkin == null ? npcTypeName + " " + I18n.get("gui.ancientwarfarenpc.skin_info.npc_type_random") : npcTypeSkin.toString().replace("ancientwarfare:skinpack/", "").replace(".png", "");
            case DEFAULT:
            default:
                return I18n.get("gui.ancientwarfarenpc.skin_info.default");
        }
    }

    public void setSkinType(SkinType skinType) {
        this.skinTypeSelected = skinType;
    }

    public SkinType getSkinType() {
        return skinTypeSelected;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        playerSkinLoaded = false;
        playerSkin = null;
    }

    public String getNpcTypeName() {
        return npcTypeName;
    }

    public void setNpcTypeName(String npcTypeName) {
        this.npcTypeName = npcTypeName;
    }

    public Optional<ResourceLocation> getNpcTypeSkin() {
        return Optional.ofNullable(npcTypeSkin);
    }

    public void resetNpcTypeSkin() {
        this.npcTypeSkin = null;
    }

    public void setNpcTypeSkin(ResourceLocation npcTypeSkin) {
        this.npcTypeSkin = npcTypeSkin;
    }

    public void setRandom(boolean random) {
        this.random = random;
    }

    public boolean isRandom() {
        return random;
    }

    public void onNpcSet(NpcBase npc) {
        loadPlayerProfile(npc);
    }

    public enum SkinType implements StringRepresentable {
        DEFAULT,
        PLAYER,
        NPC_TYPE;

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }

        public String getName() {
            return getSerializedName();
        }

        private static final Map<String, SkinType> NAME_TYPE;

        static {
            ImmutableMap.Builder<String, SkinType> builder = new ImmutableMap.Builder<>();
            for (SkinType type : values()) {
                builder.put(type.getName(), type);
            }
            NAME_TYPE = builder.build();
        }

        public static SkinType byName(String name) {
            return NAME_TYPE.getOrDefault(name, DEFAULT);
        }
    }
}
