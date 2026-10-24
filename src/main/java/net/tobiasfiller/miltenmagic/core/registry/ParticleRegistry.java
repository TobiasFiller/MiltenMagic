package net.tobiasfiller.miltenmagic.core.registry;

import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;

public class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, MiltenMagic.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> TELEPORTATION_PARTICLE =
            PARTICLES.register("teleport_particle", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> FIRE_RAIN_PARTICLE =
            PARTICLES.register("fire_rain_particle", () -> new SimpleParticleType(false));
}
