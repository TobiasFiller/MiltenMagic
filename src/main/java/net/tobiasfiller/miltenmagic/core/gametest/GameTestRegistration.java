package net.tobiasfiller.miltenmagic.core.gametest;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.tobiasfiller.miltenmagic.MiltenMagic;

@EventBusSubscriber(modid = MiltenMagic.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class GameTestRegistration {
    @SubscribeEvent
    public static void registerGameTests(final RegisterGameTestsEvent event) {
        event.register(MiltenMagicGameTests.class);
    }
}