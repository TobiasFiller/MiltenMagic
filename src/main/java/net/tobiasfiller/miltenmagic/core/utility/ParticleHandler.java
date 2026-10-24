package net.tobiasfiller.miltenmagic.core.utility;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.particle.FireRainParticle;
import net.tobiasfiller.miltenmagic.common.particle.TeleportationParticle;
import net.tobiasfiller.miltenmagic.core.registry.ParticleRegistry;

/**
 * Particle providers must be registered through {@link RegisterParticleProvidersEvent}.
 * <p>
 * FMLClientSetupEvent is too late: NeoForge fires the event from the {@code Minecraft} constructor,
 * right after {@code ParticleEngine} is created and <em>before</em> it is registered as a reload
 * listener. Only providers known at that moment get a {@code MutableSpriteSet} whose sprites are
 * bound while the particle definition JSONs are loaded. Registering later leaves
 * {@code MutableSpriteSet.sprites} null and the first spawn crashes with a NullPointerException.
 */
@EventBusSubscriber(modid = MiltenMagic.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleHandler {

    @SubscribeEvent
    public static void registerParticleProviders(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.TELEPORTATION_PARTICLE.get(), TeleportationParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.FIRE_RAIN_PARTICLE.get(), FireRainParticle.Provider::new);
    }
}