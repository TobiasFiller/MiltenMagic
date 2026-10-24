package net.tobiasfiller.miltenmagic.core.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.recipe.DyeSpellItemRecipe;

public class RecipeRegistry {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MiltenMagic.MOD_ID);

    /** Scroll/rune + dye. See {@link DyeSpellItemRecipe} and data/miltenmagic/recipe/dye_spell_item.json. */
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<DyeSpellItemRecipe>> DYE_SPELL_ITEM =
            RECIPE_SERIALIZERS.register("dye_spell_item",
                    () -> new SimpleCraftingRecipeSerializer<>(DyeSpellItemRecipe::new));
}
