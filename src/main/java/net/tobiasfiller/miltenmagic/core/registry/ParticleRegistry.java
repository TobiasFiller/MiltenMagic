package net.tobiasfiller.miltenmagic.core.registry;

import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ParticleRegistry {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MiltenMagic.MOD_ID);

    public static final RegistryObject<SimpleParticleType> TELEPORTATION_PARTICLE =
            PARTICLES.register("teleport_particle" , () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> FIRE_RAIN_PARTICLE =
            PARTICLES.register("fire_rain_particle" , () -> new SimpleParticleType(false));
}
