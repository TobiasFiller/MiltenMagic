package net.tobiasfiller.miltenmagic.core.world.feature;

import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class ModPlacedFeatures {
    public static final Holder<PlacedFeature> PATCH_SWAMPWEED =
            PlacementUtils.register("patch_swampweed",
                    ModConfiguredFeatures.PATCH_SWAMPWEED,
                    RarityFilter.onAverageOnceEvery(16),
                    InSquarePlacement.spread(),
                    PlacementUtils.HEIGHTMAP,
                    BiomeFilter.biome());
}
