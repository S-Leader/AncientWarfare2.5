package net.shadowmage.ancientwarfare.npc.compat.ebwizardry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fml.ModList;

import java.util.Optional;

/**
 * Narrow compatibility boundary for Electroblob's Wizardry Redux on 1.20.1.
 *
 * <p>Redux retains the {@code ebwizardry} namespace, but its Java API was
 * rewritten under {@code com.binaris.wizardry}. Keeping all Redux-specific
 * access behind this class prevents Alpha API changes from spreading through
 * Ancient Warfare's NPC implementation.</p>
 */
public final class WizardryReduxBridge {
    public static final String MOD_ID = "ebwizardry";
    public static final String JAVA_PACKAGE_PREFIX = "com.binaris.wizardry";

    private WizardryReduxBridge() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    /**
     * Returns true for a Redux damage type or a Redux-owned direct entity.
     */
    public static boolean isWizardryDamage(DamageSource source) {
        Optional<ResourceLocation> damageType = source.typeHolder().unwrapKey().map(key -> key.location());
        if (damageType.map(id -> MOD_ID.equals(id.getNamespace())).orElse(false)) {
            return true;
        }

        Entity direct = source.getDirectEntity();
        return direct != null && direct.getClass().getName().startsWith(JAVA_PACKAGE_PREFIX);
    }

    /**
     * Stable serialized spell key used by the port. Actual casting is bound in
     * the Redux-specific spell adapter after selecting the exact Redux Alpha jar.
     */
    public static ResourceLocation spellId(String serializedId) {
        return ResourceLocation.tryParse(serializedId);
    }
}
