package net.tobiasfiller.miltenmagic.core.world;

import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.core.world.generation.SwampweedGeneration;

@Mod.EventBusSubscriber(modid = MiltenMagic.MOD_ID)
public class ModWorldEvents {
    @SubscribeEvent
    public static void biomeLoadingEvent(final BiomeLoadingEvent event) {
        SwampweedGeneration.generateSwampweed(event);
    }
}
