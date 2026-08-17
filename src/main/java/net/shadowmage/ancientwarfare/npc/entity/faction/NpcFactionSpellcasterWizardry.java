package net.shadowmage.ancientwarfare.npc.entity.faction;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import com.binaris.wizardry.api.content.entity.living.ISpellCaster;
import com.binaris.wizardry.api.content.spell.Spell;
import com.binaris.wizardry.api.content.spell.internal.EntityCastContext;
import com.binaris.wizardry.api.content.spell.internal.SpellModifiers;
import com.binaris.wizardry.core.platform.Services;
import com.binaris.wizardry.setup.registries.Spells;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.shadowmage.ancientwarfare.core.init.AWMenuTypes;
import net.shadowmage.ancientwarfare.core.network.NetworkHandler;
import net.shadowmage.ancientwarfare.npc.compat.ebwizardry.ai.EntityAIAttackSpellImproved;

import java.util.ArrayList;
import java.util.List;

/**
 * Wizardry Redux spellcaster.
 *
 * <p>The old port only preserved spell ids for the GUI/NBT and never exposed
 * the NPC as a Redux {@link ISpellCaster}, so the configured spells could not
 * actually be cast. This implementation mirrors Redux' AbstractWizard casting
 * state while keeping AW's faction NPC hierarchy and serialized spell-id
 * format intact.</p>
 */
public class NpcFactionSpellcasterWizardry extends NpcFactionSpellcaster implements ISpellCaster {
    private static final EntityDataAccessor<String> CONTINUOUS_SPELL = SynchedEntityData.defineId(NpcFactionSpellcasterWizardry.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> SPELL_COUNTER = SynchedEntityData.defineId(NpcFactionSpellcasterWizardry.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SPELL_TARGET_ID = SynchedEntityData.defineId(NpcFactionSpellcasterWizardry.class, EntityDataSerializers.INT);

    private final List<ResourceLocation> spellIds = new ArrayList<>();
    private int healCooldown = -1;

    public NpcFactionSpellcasterWizardry(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        addWizardryAI();
    }


    private void addWizardryAI() {
        // Keep AW's home/follow/door goals, but use Redux' actual casting
        // contract for combat. This goal handles instant and continuous spells.
        tasks.addTask(3, new EntityAIAttackSpellImproved<>(this, 0.85D, 16.0F, 16, 60));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CONTINUOUS_SPELL, Spells.NONE.getLocation().toString());
        entityData.define(SPELL_COUNTER, 0);
        entityData.define(SPELL_TARGET_ID, -1);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        handleContinuousSpellOnClient();

        if (level().isClientSide || !isAlive()) {
            return;
        }

        // Preserve the port's existing passive self-heal behaviour.
        if (healCooldown > 0) {
            healCooldown--;
        } else if (healCooldown == 0 && getHealth() < getMaxHealth()) {
            heal(4.0F);
            healCooldown = -1;
        } else if (healCooldown < 0) {
            healCooldown = getHealth() < 25.0F ? 150 : 400;
        }
    }

    /**
     * Redux replays continuous spells client-side for particles/sounds. Its
     * native wizard does the same thing in AbstractWizard#aiStep; AW NPCs do
     * not inherit that class, so the equivalent hook is required here.
     */
    private void handleContinuousSpellOnClient() {
        if (!level().isClientSide) {
            return;
        }

        Spell continuousSpell = getContinuousSpell();
        int counter = getSpellCounter();
        if (continuousSpell == Spells.NONE || counter <= 0) {
            return;
        }

        int targetId = getSpellTargetId();
        if (targetId == -1) {
            return;
        }

        Entity entity = level().getEntity(targetId);
        if (entity instanceof LivingEntity target) {
            EntityCastContext context = new EntityCastContext(level(), this, InteractionHand.MAIN_HAND, counter, target, getModifiers());
            continuousSpell.cast(context);
        }
    }

    /** Stable ids used by AW's GUI/defaults/NBT. */
    public List<ResourceLocation> getSpellIds() {
        return new ArrayList<>(spellIds);
    }

    /**
     * Resolve AW's stable ids through Redux' registry only when the AI asks for
     * spells. Missing/renamed spell ids are ignored instead of breaking the NPC.
     */
    @Override
    public List<Spell> getSpells() {
        List<Spell> resolved = new ArrayList<>(spellIds.size());
        for (ResourceLocation id : spellIds) {
            Spell spell = Services.REGISTRY_UTIL.getSpell(id);
            if (spell != null && spell != Spells.NONE) {
                resolved.add(spell);
            }
        }
        return resolved;
    }

    @Override
    public SpellModifiers getModifiers() {
        // Preserve Ancient Warfare 1.12's spellcaster potency bonus.
        return new SpellModifiers().set(SpellModifiers.POTENCY, 1.5F);
    }

    @Override
    public int getAimingError(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> 7;
            case NORMAL -> 4;
            case HARD -> 1;
            default -> 7;
        };
    }

    public void setSpells(List<ResourceLocation> ids) {
        spellIds.clear();
        for (ResourceLocation id : ids) {
            if (id != null && !spellIds.contains(id)) {
                spellIds.add(id);
            }
        }
    }

    @Override
    public Spell getContinuousSpell() {
        ResourceLocation id = ResourceLocation.tryParse(entityData.get(CONTINUOUS_SPELL));
        Spell spell = id == null ? null : Services.REGISTRY_UTIL.getSpell(id);
        return spell == null ? Spells.NONE : spell;
    }

    @Override
    public void setContinuousSpell(Spell spell) {
        Spell safeSpell = spell == null ? Spells.NONE : spell;
        entityData.set(CONTINUOUS_SPELL, safeSpell.getLocation().toString());
    }

    @Override
    public int getSpellCounter() {
        return entityData.get(SPELL_COUNTER);
    }

    @Override
    public void setSpellCounter(int count) {
        entityData.set(SPELL_COUNTER, Math.max(0, count));
    }

    public int getSpellTargetId() {
        return entityData.get(SPELL_TARGET_ID);
    }

    public void setSpellTargetId(int targetId) {
        entityData.set(SPELL_TARGET_ID, targetId);
    }

    @Override
    public boolean hasAltGui() {
        return true;
    }

    @Override
    public void openAltGui(Player player) {
        AWMenuTypes.open(player, NetworkHandler.GUI_NPC_FACTION_SPELLCASTER_WIZARDRY, getId(), 0, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        writeSpellData(tag);
        tag.putInt("wizardryHealCooldown", healCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        readSpellData(tag);
        healCooldown = tag.contains("wizardryHealCooldown", Tag.TAG_INT) ? tag.getInt("wizardryHealCooldown") : -1;
    }

    @Override
    public void writeAdditionalItemData(CompoundTag tag) {
        super.writeAdditionalItemData(tag);
        writeSpellData(tag);
    }

    @Override
    public void readAdditionalItemData(CompoundTag tag) {
        super.readAdditionalItemData(tag);
        readSpellData(tag);
    }

    private void writeSpellData(CompoundTag tag) {
        ListTag list = new ListTag();
        for (ResourceLocation spell : spellIds) {
            list.add(StringTag.valueOf(spell.toString()));
        }
        tag.put("spells", list);
    }

    private void readSpellData(CompoundTag tag) {
        spellIds.clear();
        ListTag list = tag.getList("spells", Tag.TAG_STRING);
        for (Tag value : list) {
            ResourceLocation id = ResourceLocation.tryParse(value.getAsString());
            if (id != null && !spellIds.contains(id)) {
                spellIds.add(id);
            }
        }
    }
}
