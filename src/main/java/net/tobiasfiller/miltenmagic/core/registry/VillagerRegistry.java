package net.tobiasfiller.miltenmagic.core.registry;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tobiasfiller.miltenmagic.MiltenMagic;

import java.util.Set;
import java.util.stream.Collectors;

public class VillagerRegistry {
    public static final DeferredRegister<PoiType> POI_TYPES
            = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MiltenMagic.MOD_ID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS
            = DeferredRegister.create(Registries.VILLAGER_PROFESSION, MiltenMagic.MOD_ID);

    public static final ResourceKey<PoiType> FIRE_MAGE_POI_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,
            ResourceLocation.fromNamespaceAndPath(MiltenMagic.MOD_ID, "fire_mage_poi"));

    public static final DeferredHolder<PoiType, PoiType> FIRE_MAGE_POI = POI_TYPES.register("fire_mage_poi",
            () -> new PoiType(blockStates(), 1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> FIRE_MAGE =
            VILLAGER_PROFESSIONS.register("fire_mage",
                    () -> new VillagerProfession("fire_mage",
                            poIType -> poIType.is(FIRE_MAGE_POI_KEY), // held job site
                            // In 1.21.1 this is driven by the minecraft:acquirable_job_site
                            // POI tag; our POI is added to it via datapack.
                            VillagerProfession.ALL_ACQUIRABLE_JOBS,  // acquirable job site
                            ImmutableSet.of(),                       // requested items
                            ImmutableSet.of(Blocks.LECTERN),         // secondary POI
                            SoundEvents.VILLAGER_WORK_LIBRARIAN));

    /**
     * In 1.21.1 {@link PoiManager} discovers the block states belonging to a {@link PoiType} by itself,
     * so no manual {@code registerBlockStates} call is needed anymore.
     */
    private static Set<BlockState> blockStates() {
        return BlockRegistry.MAGICAL_LECTERN.get().getStateDefinition().getPossibleStates().stream()
                .collect(Collectors.toUnmodifiableSet());
    }
}