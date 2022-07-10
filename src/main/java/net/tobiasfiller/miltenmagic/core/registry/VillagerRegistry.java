package net.tobiasfiller.miltenmagic.core.registry;

import com.google.common.collect.ImmutableSet;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tobiasfiller.miltenmagic.MiltenMagic;

import java.lang.reflect.InvocationTargetException;

public class VillagerRegistry {
    public static final DeferredRegister<PoiType> POI_TYPES
            = DeferredRegister.create(ForgeRegistries.POI_TYPES, MiltenMagic.MOD_ID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS
            = DeferredRegister.create(ForgeRegistries.PROFESSIONS, MiltenMagic.MOD_ID);

    public static final RegistryObject<PoiType> FIRE_MAGE_POI = POI_TYPES.register("fire_mage_poi",
            () -> new PoiType("fire_mage_poi",
                    PoiType.getBlockStates(Blocks.MAGMA_BLOCK), 1, 1));

    public static final RegistryObject<VillagerProfession> FIRE_MAGE =
            VILLAGER_PROFESSIONS.register("fire_mage",
                    () -> new VillagerProfession("fire_mage", FIRE_MAGE_POI.get(),
                            ImmutableSet.of(), ImmutableSet.of(Blocks.LECTERN), SoundEvents.VILLAGER_WORK_LIBRARIAN));


    public static void registerPOIs() {
        try {
            ObfuscationReflectionHelper.findMethod(PoiType.class,
                    "registerBlockStates", PoiType.class).invoke(null, FIRE_MAGE_POI.get());
        } catch(InvocationTargetException | IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }
}
