package net.tobiasfiller.miltenmagic.core.registry;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.blockentity.StonePedestalBlockEntity;

/**
 * In 1.21.1 block capabilities are no longer exposed by overriding
 * {@code BlockEntity#getCapability}; they have to be registered explicitly
 * through {@link RegisterCapabilitiesEvent}.
 */
@EventBusSubscriber(modid = MiltenMagic.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CapabilityRegistry {

    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                BlockEntityRegistry.STONE_PEDESTAL_BLOCK_ENTITY.get(),
                (be, side) -> be.getItemHandler());
    }
}