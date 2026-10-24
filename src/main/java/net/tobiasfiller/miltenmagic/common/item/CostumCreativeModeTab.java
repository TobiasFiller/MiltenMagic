package net.tobiasfiller.miltenmagic.common.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.core.registry.ItemRegistry;

public class CostumCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MiltenMagic.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB_MILTEN_MAGIC = CREATIVE_MODE_TABS.register("miltenmagictab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.miltenmagictab"))
            .icon(() -> new ItemStack(Items.LAPIS_LAZULI))
            // Without a DisplayItemsGenerator the tab is built but stays empty,
            // so it never shows anything in the creative inventory.
            .displayItems((parameters, output) -> {
                // Scrolls
                output.accept(ItemRegistry.TELEPORTATION_SCROLL.get());
                output.accept(ItemRegistry.LEVITATION_SCROLL.get());
                output.accept(ItemRegistry.FIRE_ARROW_SCROLL.get());
                output.accept(ItemRegistry.FIRE_CHARGE_SCROLL.get());
                output.accept(ItemRegistry.FIRE_RAINE_SCROLL.get());
                output.accept(ItemRegistry.MAGNETIC_SCROLL.get());
                output.accept(ItemRegistry.ABSORPTION_SCROLL.get());
                output.accept(ItemRegistry.RESISTANCE_SCROLL.get());
                output.accept(ItemRegistry.MAGICAL_PROTECTION_SCROLL.get());

                // Runes
                output.accept(ItemRegistry.TELEPORTATION_RUNE.get());

                // Swampweed
                output.accept(ItemRegistry.GREEN_NOVICE.get());
                output.accept(ItemRegistry.BLACK_WISE.get());
                output.accept(ItemRegistry.DREAM_CALL.get());

                // Blocks
                output.accept(ItemRegistry.TELEPORTATION_PLATFORM.get());
                output.accept(ItemRegistry.MAGICAL_LECTERN.get());
                output.accept(ItemRegistry.STONE_PEDESTAL.get());
                output.accept(ItemRegistry.SWAMPWEED.get());
                output.accept(ItemRegistry.TABLE.get());
                output.accept(ItemRegistry.ACACIA_TABLE.get());
                output.accept(ItemRegistry.BIRCH_TABLE.get());
                output.accept(ItemRegistry.CRIMSON_TABLE.get());
                output.accept(ItemRegistry.DARK_OAK_TABLE.get());
                output.accept(ItemRegistry.JUNGLE_TABLE.get());
                output.accept(ItemRegistry.OAK_TABLE.get());
                output.accept(ItemRegistry.SPRUCE_TABLE.get());
                output.accept(ItemRegistry.WARPED_TABLE.get());
            })
            .build());
}