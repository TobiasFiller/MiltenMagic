package net.tobiasfiller.miltenmagic.core.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tobiasfiller.miltenmagic.MiltenMagic;

public class SoundRegistry {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MiltenMagic.MOD_ID);

    public static final RegistryObject<SoundEvent> SWAMPWEED_SMOKE =
            SOUND_EVENTS.register("swampweed_smoking",
                    () -> new SoundEvent(new ResourceLocation(MiltenMagic.MOD_ID,"swampweed_smoking")));
}
