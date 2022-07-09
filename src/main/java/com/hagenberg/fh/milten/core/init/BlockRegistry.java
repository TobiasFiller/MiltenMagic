package com.hagenberg.fh.milten.core.init;

import com.hagenberg.fh.milten.Milten;
import com.hagenberg.fh.milten.common.block.TeleportationPlatformBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegistry {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Milten.Mod_ID);

    public static final RegistryObject<Block> TELEPORTATION_PLATFORM_BLOCK = BLOCKS.register("teleportation_platform", TeleportationPlatformBlock::new);

}
