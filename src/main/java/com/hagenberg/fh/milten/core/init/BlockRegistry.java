package com.hagenberg.fh.milten.core.init;

import com.hagenberg.fh.milten.Milten;
import com.hagenberg.fh.milten.common.block.TeleportationPlatformBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockRegistry {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Milten.Mod_ID);

    public static final RegistryObject<Block> TELEPORTATION_PLATFORM_BLOCK = BLOCKS.register("teleportation_platform", TeleportationPlatformBlock::new);

}
