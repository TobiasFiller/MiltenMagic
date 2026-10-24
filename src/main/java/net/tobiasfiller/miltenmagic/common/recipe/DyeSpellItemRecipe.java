package net.tobiasfiller.miltenmagic.common.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.tobiasfiller.miltenmagic.core.registry.ItemRegistry;
import net.tobiasfiller.miltenmagic.core.registry.RecipeRegistry;

/**
 * Dyes a teleportation scroll or rune with exactly one dye.
 * <p>
 * The vanilla {@code ArmorDyeRecipe} cannot be reused here: it only accepts items in the
 * {@code #minecraft:dyeable} tag and it averages the new colours with the old one, so dyeing
 * twice would blend instead of replace. This recipe replaces the stored colour outright and
 * copies the input stack, which keeps the platform link in {@code CUSTOM_DATA} intact.
 * <p>
 * The colour itself is only read by the client ({@code ClientSetup#onRegisterItemColors}), which
 * tints the glyph overlay of the item model.
 */
public class DyeSpellItemRecipe extends CustomRecipe {

    public DyeSpellItemRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack target = ItemStack.EMPTY;
        boolean hasDye = false;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (isDyeableSpellItem(stack)) {
                if (!target.isEmpty()) {
                    return false;
                }
                target = stack;
            } else if (stack.getItem() instanceof DyeItem) {
                // Exactly one dye, so a second dye cannot silently mix into the first colour.
                if (hasDye) {
                    return false;
                }
                hasDye = true;
            } else {
                return false;
            }
        }

        return !target.isEmpty() && hasDye;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack target = ItemStack.EMPTY;
        DyeItem dye = null;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (isDyeableSpellItem(stack)) {
                if (!target.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                target = stack;
            } else if (stack.getItem() instanceof DyeItem dyeItem) {
                if (dye != null) {
                    return ItemStack.EMPTY;
                }
                dye = dyeItem;
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (target.isEmpty() || dye == null) {
            return ItemStack.EMPTY;
        }

        int rgb = dye.getDyeColor().getTextureDiffuseColor();
        ItemStack result = target.copyWithCount(1);
        // Store the colour with a zero alpha like vanilla does; DyedItemColor#getOrDefault makes
        // it opaque again for rendering and the tooltip prints "#RRGGBB" from the low 24 bits.
        result.set(DataComponents.DYED_COLOR, new DyedItemColor(
                FastColor.ARGB32.color(0,
                        FastColor.ARGB32.red(rgb),
                        FastColor.ARGB32.green(rgb),
                        FastColor.ARGB32.blue(rgb)),
                true));
        return result;
    }

    private static boolean isDyeableSpellItem(ItemStack stack) {
        return stack.is(ItemRegistry.TELEPORTATION_SCROLL.get())
                || stack.is(ItemRegistry.TELEPORTATION_RUNE.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeRegistry.DYE_SPELL_ITEM.get();
    }
}
