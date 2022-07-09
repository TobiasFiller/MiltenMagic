package com.hagenberg.fh.milten.utility;

import com.hagenberg.fh.milten.Milten;
import com.hagenberg.fh.milten.common.particle.TeleportationParticle;
import com.hagenberg.fh.milten.core.init.ParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Milten.Mod_ID,bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerParticle(ParticleFactoryRegisterEvent event) {
        Minecraft.getInstance().particleEngine.register(ParticleRegistry.TELEPORTATION_PARTICLE.get(), TeleportationParticle.Provider::new);
    }
}
