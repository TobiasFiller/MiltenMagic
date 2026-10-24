package net.tobiasfiller.miltenmagic;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.tobiasfiller.miltenmagic.common.item.CostumCreativeModeTab;
import net.tobiasfiller.miltenmagic.core.registry.*;
import org.slf4j.Logger;

@Mod(MiltenMagic.MOD_ID)
public class MiltenMagic {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "miltenmagic";

    public MiltenMagic(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(this::setup);

        ItemRegistry.ITEMS.register(modBus);
        BlockRegistry.BLOCKS.register(modBus);
        BlockEntityRegistry.BLOCK_ENTITIES.register(modBus);
        EntityRegistry.ENTITY_TYPES.register(modBus);
        MobEffectRegistry.MOB_EFFECT.register(modBus);
        ParticleRegistry.PARTICLES.register(modBus);
        RecipeRegistry.RECIPE_SERIALIZERS.register(modBus);
        SoundRegistry.SOUND_EVENTS.register(modBus);
        VillagerRegistry.POI_TYPES.register(modBus);
        VillagerRegistry.VILLAGER_PROFESSIONS.register(modBus);
        CostumCreativeModeTab.CREATIVE_MODE_TABS.register(modBus);

        // The swampweed configured/placed feature is datapack-driven (see
        // data/miltenmagic/worldgen/...), because worldgen registries cannot be
        // populated from code since 1.21.

        // No NeoForge.EVENT_BUS.register(this) here: this class declares no
        // @SubscribeEvent methods and the event bus rejects listener classes without any.
    }

    private void setup(final FMLCommonSetupEvent event) {
        LOGGER.info("MiltenMagic common setup");
    }
}