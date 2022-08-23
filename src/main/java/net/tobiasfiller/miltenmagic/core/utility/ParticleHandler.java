package net.tobiasfiller.miltenmagic.core.utility;

import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.particle.FireRainParticle;
import net.tobiasfiller.miltenmagic.common.particle.TeleportationParticle;
import net.tobiasfiller.miltenmagic.core.registry.ParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MiltenMagic.MOD_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticle(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particleEngine.register(ParticleRegistry.TELEPORTATION_PARTICLE.get(), TeleportationParticle.Provider::new);
        Minecraft.getInstance().particleEngine.register(ParticleRegistry.FIRE_RAIN_PARTICLE.get(), FireRainParticle.Provider::new);
    }


}
