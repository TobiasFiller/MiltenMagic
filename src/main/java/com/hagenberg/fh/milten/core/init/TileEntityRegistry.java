package com.hagenberg.fh.milten.core.init;

import com.hagenberg.fh.milten.Milten;
import com.hagenberg.fh.milten.common.tileEntity.TeleportationPlatformTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TileEntityRegistry {

    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Milten.Mod_ID);

    public static final RegistryObject<TileEntityType<TeleportationPlatformTileEntity>> TELEPORTATION_PLATFORM_TILE_ENTITY =
            TILE_ENTITIES.register("teleportation_platform_tile_entity", () ->
                    TileEntityType.Builder.create(TeleportationPlatformTileEntity::new, BlockRegistry.TELEPORTATION_PLATFORM_BLOCK.get())
                    .build(null));
}
