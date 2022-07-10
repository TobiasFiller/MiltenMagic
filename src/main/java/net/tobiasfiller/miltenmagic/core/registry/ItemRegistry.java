package net.tobiasfiller.miltenmagic.core.registry;

import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.item.CostumCreativeModeTab;
import net.tobiasfiller.miltenmagic.common.item.TeleportationSpellItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MiltenMagic.MOD_ID);

    public static final RegistryObject<Item> TELEPORTATION_SCROLL =
            ITEMS.register("teleportation_scroll",()->new TeleportationSpellItem(true));

    public static final RegistryObject<Item> TELEPORTATION_RUNE =
            ITEMS.register("teleportation_rune",()-> new TeleportationSpellItem(false));

    public static final RegistryObject<Item> TELEPORTATION_PLATFORM = ITEMS.register("teleportation_platform", () -> new BlockItem(
            BlockRegistry.TELEPORTATION_PLATFORM_BLOCK.get(),new Item.Properties().tab(CostumCreativeModeTab.TAB_MILTEN_MAGIC)));
}
