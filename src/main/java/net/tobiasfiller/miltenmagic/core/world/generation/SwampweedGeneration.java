package net.tobiasfiller.miltenmagic.core.world.generation;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.tobiasfiller.miltenmagic.core.world.feature.ModPlacedFeatures;

import java.util.List;

public class SwampweedGeneration {
    public static void generateSwampweed(final BiomeLoadingEvent event) {
        if (event.getCategory().equals(Biome.BiomeCategory.SWAMP)){
            List<Holder<PlacedFeature>> base =
                    event.getGeneration().getFeatures(GenerationStep.Decoration.VEGETAL_DECORATION);

            base.add(ModPlacedFeatures.PATCH_SWAMPWEED);
        }
    }
}
