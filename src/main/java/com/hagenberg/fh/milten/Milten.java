package com.hagenberg.fh.milten;

import com.hagenberg.fh.milten.core.init.BlockRegistry;
import com.hagenberg.fh.milten.core.init.ItemRegistry;
import com.hagenberg.fh.milten.core.init.ParticleRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Milten.Mod_ID)
public class Milten {
    // Directly reference a log4j logger.
    public static final boolean Debug = true;
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String Mod_ID = "milten";

    public Milten() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ItemRegistry.ITEMS.register(bus);
        BlockRegistry.BLOCKS.register(bus);
        ParticleRegistry.PARTICLES.register(bus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }
}