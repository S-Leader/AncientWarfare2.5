package net.shadowmage.ancientwarfare.npc.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.shadowmage.ancientwarfare.npc.AncientWarfareNPC;

import java.util.HashMap;
import java.util.Map;

public class AWNPCSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AncientWarfareNPC.MOD_ID);

    public static Map<String, SoundEvent> npcSoundEvents = new HashMap<>();

    public static final SoundEvent BARBARIAN_ATTACK = createSoundEvent("barbarian_attack");
    public static final SoundEvent BARBARIAN_HURT = createSoundEvent("barbarian_hurt");
    public static final SoundEvent BEAST_ATTACK = createSoundEvent("beast_attack");
    public static final SoundEvent BEAST_DEATH = createSoundEvent("beast_death");
    public static final SoundEvent BEAST_HURT = createSoundEvent("beast_hurt");
    public static final SoundEvent BRIGAND_ATTACK = createSoundEvent("brigand_attack");
    public static final SoundEvent BRIGAND_DEATH = createSoundEvent("brigand_death");
    public static final SoundEvent BRIGAND_HURT = createSoundEvent("brigand_hurt");
    public static final SoundEvent COVEN_DRYAD_DEATH = createSoundEvent("coven_dryad_death");
    public static final SoundEvent COVEN_DRYAD_HURT = createSoundEvent("coven_dryad_hurt");
    public static final SoundEvent COVEN_FAMILIAR_ATTACK = createSoundEvent("coven_familiar_attack");
    public static final SoundEvent COVEN_FAMILIAR_DEATH = createSoundEvent("coven_familiar_death");
    public static final SoundEvent COVEN_FAMILIAR_HURT = createSoundEvent("coven_familiar_hurt");
    public static final SoundEvent COVEN_PUPPET_ATTACK = createSoundEvent("coven_puppet_attack");
    public static final SoundEvent COVEN_PUPPET_DEATH = createSoundEvent("coven_puppet_death");
    public static final SoundEvent COVEN_PUPPET_HURT = createSoundEvent("coven_puppet_hurt");
    public static final SoundEvent COVEN_SATYR_DEATH = createSoundEvent("coven_satyr_death");
    public static final SoundEvent COVEN_SATYR_HURT = createSoundEvent("coven_satyr_hurt");
    public static final SoundEvent COVEN_SCARECROW_ATTACK = createSoundEvent("coven_scarecrow_attack");
    public static final SoundEvent COVEN_SCARECROW_DEATH = createSoundEvent("coven_scarecrow_death");
    public static final SoundEvent COVEN_SCARECROW_HURT = createSoundEvent("coven_scarecrow_hurt");
    public static final SoundEvent COVEN_WITCH_ATTACK = createSoundEvent("coven_witch_attack");
    public static final SoundEvent COVEN_WITCH_DEATH = createSoundEvent("coven_witch_death");
    public static final SoundEvent COVEN_WITCH_HURT = createSoundEvent("coven_witch_hurt");
    public static final SoundEvent DWARF_ATTACK = createSoundEvent("dwarf_attack");
    public static final SoundEvent DWARF_DEATH = createSoundEvent("dwarf_death");
    public static final SoundEvent DWARF_HURT = createSoundEvent("dwarf_hurt");
    public static final SoundEvent ELF_ATTACK = createSoundEvent("elf_attack");
    public static final SoundEvent ELF_DEATH = createSoundEvent("elf_death");
    public static final SoundEvent ELF_HURT = createSoundEvent("elf_hurt");
    public static final SoundEvent ENT_ATTACK = createSoundEvent("ent_attack");
    public static final SoundEvent ENT_DEATH = createSoundEvent("ent_death");
    public static final SoundEvent ENT_HURT = createSoundEvent("ent_hurt");
    public static final SoundEvent GARGOYLE_ATTACK = createSoundEvent("gargoyle_attack");
    public static final SoundEvent GARGOYLE_DEATH = createSoundEvent("gargoyle_death");
    public static final SoundEvent GARGOYLE_HURT = createSoundEvent("gargoyle_hurt");
    public static final SoundEvent GIANT_ATTACK = createSoundEvent("giant_attack");
    public static final SoundEvent GIANT_DEATH = createSoundEvent("giant_death");
    public static final SoundEvent GIANT_HURT = createSoundEvent("giant_hurt");
    public static final SoundEvent GNOME_ATTACK = createSoundEvent("gnome_attack");
    public static final SoundEvent GNOME_DEATH = createSoundEvent("gnome_death");
    public static final SoundEvent GNOME_HURT = createSoundEvent("gnome_hurt");
    public static final SoundEvent GREMLIN_ATTACK = createSoundEvent("gremlin_attack");
    public static final SoundEvent GREMLIN_DEATH = createSoundEvent("gremlin_death");
    public static final SoundEvent GREMLIN_HURT = createSoundEvent("gremlin_hurt");
    public static final SoundEvent HOBBIT_ATTACK = createSoundEvent("hobbit_attack");
    public static final SoundEvent HOBBIT_DEATH = createSoundEvent("hobbit_death");
    public static final SoundEvent HOBBIT_HURT = createSoundEvent("hobbit_hurt");
    public static final SoundEvent HUMAN_ATTACK = createSoundEvent("human_attack");
    public static final SoundEvent HUMAN_DEATH = createSoundEvent("human_death");
    public static final SoundEvent HUMAN_FEMALE_ATTACK = createSoundEvent("human_female_attack");
    public static final SoundEvent HUMAN_FEMALE_DEATH = createSoundEvent("human_female_death");
    public static final SoundEvent HUMAN_FEMALE_HURT = createSoundEvent("human_female_hurt");
    public static final SoundEvent HUMAN_HURT = createSoundEvent("human_hurt");
    public static final SoundEvent ISHTARI_ANUBITE_ATTACK = createSoundEvent("ishtari_anubite_attack");
    public static final SoundEvent ISHTARI_ANUBITE_DEATH = createSoundEvent("ishtari_anubite_death");
    public static final SoundEvent ISHTARI_ANUBITE_HURT = createSoundEvent("ishtari_anubite_hurt");
    public static final SoundEvent ISHTARI_MUMMY_ATTACK = createSoundEvent("ishtari_mummy_attack");
    public static final SoundEvent ISHTARI_MUMMY_DEATH = createSoundEvent("ishtari_mummy_death");
    public static final SoundEvent ISHTARI_MUMMY_HURT = createSoundEvent("ishtari_mummy_hurt");
    public static final SoundEvent ISHTARI_PHAROAH_ATTACK = createSoundEvent("ishtari_pharoah_attack");
    public static final SoundEvent ISHTARI_PHAROAH_DEATH = createSoundEvent("ishtari_pharoah_death");
    public static final SoundEvent ISHTARI_PHAROAH_HURT = createSoundEvent("ishtari_pharoah_hurt");
    public static final SoundEvent KLOWN_ATTACK = createSoundEvent("klown_attack");
    public static final SoundEvent KLOWN_DEATH = createSoundEvent("klown_death");
    public static final SoundEvent KLOWN_HURT = createSoundEvent("klown_hurt");
    public static final SoundEvent KOBOLD_ATTACK = createSoundEvent("kobold_attack");
    public static final SoundEvent KOBOLD_DEATH = createSoundEvent("kobold_death");
    public static final SoundEvent KOBOLD_HURT = createSoundEvent("kobold_hurt");
    public static final SoundEvent KONG_APE_ATTACK = createSoundEvent("kong_ape_attack");
    public static final SoundEvent KONG_APE_DEATH = createSoundEvent("kong_ape_death");
    public static final SoundEvent KONG_APE_HURT = createSoundEvent("kong_ape_hurt");
    public static final SoundEvent LIZARDMAN_AMBIENT = createSoundEvent("lizardman_ambient");
    public static final SoundEvent LIZARDMAN_ATTACK = createSoundEvent("lizardman_attack");
    public static final SoundEvent LIZARDMAN_DEATH = createSoundEvent("lizardman_death");
    public static final SoundEvent LIZARDMAN_HURT = createSoundEvent("lizardman_hurt");
    public static final SoundEvent MALICE_ATTACK = createSoundEvent("malice_attack");
    public static final SoundEvent MALICE_DEATH = createSoundEvent("malice_death");
    public static final SoundEvent MALICE_HURT = createSoundEvent("malice_hurt");
    public static final SoundEvent MONSTER_ATTACK = createSoundEvent("monster_attack");
    public static final SoundEvent MONSTER_DEATH = createSoundEvent("monster_death");
    public static final SoundEvent MONSTER_HURT = createSoundEvent("monster_hurt");
    public static final SoundEvent NORSKA_ATTACK = createSoundEvent("norska_attack");
    public static final SoundEvent NORSKA_DEATH = createSoundEvent("norska_death");
    public static final SoundEvent NORSKA_HURT = createSoundEvent("norska_hurt");
    public static final SoundEvent ORC_AMBIENT = createSoundEvent("orc_ambient");
    public static final SoundEvent ORC_ATTACK = createSoundEvent("orc_attack");
    public static final SoundEvent ORC_DEATH = createSoundEvent("orc_death");
    public static final SoundEvent ORC_HURT = createSoundEvent("orc_hurt");
    public static final SoundEvent ORC_URUK_ATTACK = createSoundEvent("orc_uruk_attack");
    public static final SoundEvent ORC_URUK_DEATH = createSoundEvent("orc_uruk_death");
    public static final SoundEvent ORC_URUK_HURT = createSoundEvent("orc_uruk_hurt");
    public static final SoundEvent PIRATE_AMBIENT = createSoundEvent("pirate_ambient");
    public static final SoundEvent PIRATE_ATTACK = createSoundEvent("pirate_attack");
    public static final SoundEvent PIRATE_DEATH = createSoundEvent("pirate_death");
    public static final SoundEvent PIRATE_HURT = createSoundEvent("pirate_hurt");
    public static final SoundEvent SKELETON_DEATH = createSoundEvent("skeleton_death");
    public static final SoundEvent SKELETON_HURT = createSoundEvent("skeleton_hurt");
    public static final SoundEvent VAMPIRE_ATTACK = createSoundEvent("vampire_attack");
    public static final SoundEvent VAMPIRE_BOSS_ATTACK = createSoundEvent("vampire_boss_attack");
    public static final SoundEvent VAMPIRE_BOSS_DEATH = createSoundEvent("vampire_boss_death");
    public static final SoundEvent VAMPIRE_BOSS_HURT = createSoundEvent("vampire_boss_hurt");
    public static final SoundEvent VAMPIRE_BRIDE_ATTACK = createSoundEvent("vampire_bride_attack");
    public static final SoundEvent VAMPIRE_BRIDE_DEATH = createSoundEvent("vampire_bride_death");
    public static final SoundEvent VAMPIRE_BRIDE_HURT = createSoundEvent("vampire_bride_hurt");
    public static final SoundEvent VAMPIRE_DEATH = createSoundEvent("vampire_death");
    public static final SoundEvent VAMPIRE_HURT = createSoundEvent("vampire_hurt");
    public static final SoundEvent ZOMBIE_AMBIENT = createSoundEvent("zombie_ambient");
    public static final SoundEvent ZOMBIE_ATTACK = createSoundEvent("zombie_attack");
    public static final SoundEvent ZOMBIE_DEATH = createSoundEvent("zombie_death");
    public static final SoundEvent ZOMBIE_HURT = createSoundEvent("zombie_hurt");

    static {
        createSoundEvent("bard.tune1");
        createSoundEvent("bard.tune2");
        createSoundEvent("bard.tune3");
        createSoundEvent("bard.tune4");
        createSoundEvent("bard.tune5");
        createSoundEvent("bard.tune6");
        createSoundEvent("teleport_in");
        createSoundEvent("teleport_out");
        addAlternativeSoundReferences();
    }

    private AWNPCSounds() {
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }

    private static SoundEvent createSoundEvent(String soundName) {
        ResourceLocation registryName = new ResourceLocation(AncientWarfareNPC.MOD_ID, soundName);
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(registryName);
        SOUNDS.register(soundName, () -> soundEvent);
        npcSoundEvents.put(soundName, soundEvent);
        return soundEvent;
    }

    public static boolean isValidSound(String sound) {
        return npcSoundEvents.containsKey(sound);
    }

    public static void addAlternativeSoundReferences() {
        npcSoundEvents.put("barbarian_death", HUMAN_DEATH);
    }

    public static SoundEvent getSoundEventFromString(String name) {
        name = name.toLowerCase();
        return npcSoundEvents.get(name);
    }
}