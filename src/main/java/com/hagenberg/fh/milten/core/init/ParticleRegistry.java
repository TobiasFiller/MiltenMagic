package com.hagenberg.fh.milten.core.init;

import com.hagenberg.fh.milten.Milten;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ParticleRegistry {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Milten.Mod_ID);

    public static final RegistryObject<BasicParticleType> TELEPORTATION_PARTICLE =
            PARTICLES.register("teleport_particle" , () -> new BasicParticleType(false));
}
