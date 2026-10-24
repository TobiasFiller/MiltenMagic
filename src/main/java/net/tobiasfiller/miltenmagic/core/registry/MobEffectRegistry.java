package net.tobiasfiller.miltenmagic.core.registry;

import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.mobeffect.MagicalProtectionMobEffect;
import net.tobiasfiller.miltenmagic.common.mobeffect.MagneticMobEffect;

public class MobEffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECT = DeferredRegister.create(Registries.MOB_EFFECT, MiltenMagic.MOD_ID);

    public static final DeferredHolder<MobEffect, MobEffect> MAGNETIC_MOB_EFFECT = MOB_EFFECT.register("magnetic", MagneticMobEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> MAGICAL_PROTECTION_MOB_EFFECT = MOB_EFFECT.register("magical_protection", MagicalProtectionMobEffect::new);
}
