package net.tobiasfiller.miltenmagic.core.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.mobeffect.MagneticMobEffect;

public class MobEffectRegistry {

    public static final DeferredRegister<MobEffect> MOB_EFFECT = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MiltenMagic.MOD_ID);

    public static final RegistryObject<MobEffect> MAGNETIC_MOB_EFFECT = MOB_EFFECT.register("magnetic", MagneticMobEffect::new);
}
