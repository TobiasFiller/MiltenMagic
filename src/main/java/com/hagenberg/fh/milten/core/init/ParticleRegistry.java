package com.hagenberg.fh.milten.core.init;

import com.hagenberg.fh.milten.Milten;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ParticleRegistry {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Milten.Mod_ID);

    public static final RegistryObject<SimpleParticleType> TELEPORTATION_PARTICLE =
            PARTICLES.register("teleport_particle" , () -> new SimpleParticleType(false));
}
