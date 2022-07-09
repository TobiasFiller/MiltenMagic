package com.hagenberg.fh.milten.core.init;

import com.hagenberg.fh.milten.Milten;
import com.hagenberg.fh.milten.common.item.TeleportationRuneItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemRegistry {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Milten.Mod_ID);

    public static final RegistryObject<Item> TELEPORTATION_CRYSTAL =
            ITEMS.register("teleportation_crystal",()-> new Item(new Item.Properties().tab(CreativeModeTab.TAB_TRANSPORTATION)));

    public static final RegistryObject<Item> TELEPORTATION_SCROLL =
            ITEMS.register("teleportation_scroll",()->new TeleportationRuneItem(true));

    public static final RegistryObject<Item> TELEPORTATION_RUNE =
            ITEMS.register("teleportation_rune",()-> new TeleportationRuneItem(false));

    public static final RegistryObject<Item> TELEPORTATION_PLATFORM = ITEMS.register("teleportation_platform", () -> new BlockItem(
            BlockRegistry.TELEPORTATION_PLATFORM_BLOCK.get(),new Item.Properties().tab(CreativeModeTab.TAB_BUILDING_BLOCKS)));
}
