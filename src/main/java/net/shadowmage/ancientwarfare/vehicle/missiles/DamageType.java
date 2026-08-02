package net.shadowmage.ancientwarfare.vehicle.missiles;

import net.minecraft.core.Holder;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class DamageType extends DamageSource {

    private final boolean fireDamage;
    private final boolean explosionDamage;

    public static final DamageSource fireMissile = new DamageType("dmg.firemissile", true, false);
    public static final DamageSource explosiveMissile = new DamageType("dmg.explosivemissile", true, false);
    public static final DamageSource genericMissile = new DamageType("dmg.genericmissile", false, false);
    public static final DamageSource piercingMissile = new DamageType("dmg.piercingmissile", false, false);
    public static final DamageSource batteringDamage = new DamageType("dmg.battering", false, false);

    protected DamageType(String par1Str, boolean fire, boolean explosion) {
        super(holderFor(par1Str, fire));
        this.fireDamage = fire;
        this.explosionDamage = explosion;
    }

    protected DamageType(String type, boolean fire, boolean explosion, @Nullable Entity source) {
        super(holderFor(type, fire), source);
        this.fireDamage = fire;
        this.explosionDamage = explosion;
    }

    private static Holder<net.minecraft.world.damagesource.DamageType> holderFor(String msgId, boolean fire) {
        return Holder.direct(new net.minecraft.world.damagesource.DamageType(msgId, 0.1F, fire ? DamageEffects.BURNING : DamageEffects.HURT));
    }

    public boolean isFireDamage() {
        return fireDamage;
    }

    public boolean isExplosion() {
        return explosionDamage;
    }

    public static DamageSource causeEntityMissileDamage(Entity attacker, boolean fire, boolean expl) {
        return new DamageType("AWMissile", fire, expl, attacker);
    }

    @Override
    public Component getLocalizedDeathMessage(LivingEntity par1EntityLivingBase) {
        LivingEntity entitylivingbase1 = par1EntityLivingBase.getKillCredit();
        String s = "death.attack." + this.getMsgId();
        String s1 = s + ".player";
        return entitylivingbase1 != null && Language.getInstance().has(s1) ? Component.translatable(s1, par1EntityLivingBase.getDisplayName(),
                entitylivingbase1.getDisplayName()) : Component.translatable(s, par1EntityLivingBase.getDisplayName());
    }
}
