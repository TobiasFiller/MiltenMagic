package com.hagenberg.fh.milten.core.init;

import com.hagenberg.fh.milten.Milten;
import com.hagenberg.fh.milten.common.item.TeleportationRuneItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemRegistry {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Milten.Mod_ID);

    public static final RegistryObject<Item> TELEPORTATION_CRYSTAL =
            ITEMS.register("teleportation_crystal",()-> new Item(new Item.Properties().group(ItemGroup.TRANSPORTATION)));

    public static final RegistryObject<Item> TELEPORTATION_SCROLL =
            ITEMS.register("teleportation_scroll",()->new TeleportationRuneItem(true));

    public static final RegistryObject<Item> TELEPORTATION_RUNE =
            ITEMS.register("teleportation_rune",()-> new TeleportationRuneItem(false));

    public static final RegistryObject<Item> TELEPORTATION_PLATFORM = ITEMS.register("teleportation_platform", () -> new BlockItem(
            BlockRegistry.TELEPORTATION_PLATFORM_BLOCK.get(),new Item.Properties().group(ItemGroup.BUILDING_BLOCKS)));
}
