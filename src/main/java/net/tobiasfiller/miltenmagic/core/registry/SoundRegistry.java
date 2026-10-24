package net.tobiasfiller.miltenmagic.core.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;
import net.tobiasfiller.miltenmagic.MiltenMagic;

public class SoundRegistry {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MiltenMagic.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> SWAMPWEED_SMOKE =
            SOUND_EVENTS.register("swampweed_smoking",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MiltenMagic.MOD_ID,"swampweed_smoking")));

    public static final DeferredHolder<SoundEvent, SoundEvent> FIRE_ARROW =
            SOUND_EVENTS.register("fire_arrow",
                    () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MiltenMagic.MOD_ID,"fire_arrow")));
}
