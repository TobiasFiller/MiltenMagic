package net.tobiasfiller.miltenmagic.core.registry;

import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.block.TeleportationPlatformBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockRegistry {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MiltenMagic.MOD_ID);

    public static final RegistryObject<Block> TELEPORTATION_PLATFORM_BLOCK = BLOCKS.register("teleportation_platform", TeleportationPlatformBlock::new);

}
