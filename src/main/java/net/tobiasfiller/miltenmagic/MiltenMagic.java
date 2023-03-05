package net.tobiasfiller.miltenmagic;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.tobiasfiller.miltenmagic.core.registry.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MiltenMagic.MOD_ID)
public class MiltenMagic {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "miltenmagic";
    IEventBus bus;

    public MiltenMagic() {
        this.bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(this::setup);

        //Mod Registry's
        ItemRegistry.ITEMS.register(bus);
        BlockRegistry.BLOCKS.register(bus);
        BlockEntityRegistry.BLOCK_ENTITIES.register(bus);
        EntityRegistry.ENTITY_TYPES.register(bus);
        MobEffectRegistry.MOB_EFFECT.register(bus);
        ParticleRegistry.PARTICLES.register(bus);
        SoundRegistry.SOUND_EVENTS.register(bus);
        VillagerRegistry.POI_TYPES.register(bus);
        VillagerRegistry.VILLAGER_PROFESSIONS.register(bus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(VillagerRegistry::registerPOIs);
    }
}