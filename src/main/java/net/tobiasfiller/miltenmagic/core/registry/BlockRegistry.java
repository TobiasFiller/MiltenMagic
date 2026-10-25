package net.tobiasfiller.miltenmagic.core.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;


public class BlockRegistry {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MiltenMagic.MOD_ID);

    public static final DeferredHolder<Block, Block> TELEPORTATION_PLATFORM_BLOCK =
            BLOCKS.register("teleportation_platform", () -> new TeleportationPlatformBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.5F, 6.0F)
                            .sound(SoundType.STONE)));

    public static final DeferredHolder<Block, Block> MAGICAL_LECTERN =
            BLOCKS.register("magical_lectern", () -> new MagicalLecternBlock(
                    BlockBehaviour.Properties.of()
                            .strength(2.5F)
                            .sound(SoundType.METAL)
                            .noOcclusion()));

    public static final DeferredHolder<Block, Block> STONE_PEDESTAL =
            BLOCKS.register("stone_pedestal", () -> new StonePedestalBlock(
                    BlockBehaviour.Properties.of()
                            .destroyTime(1.2f)
                            .sound(SoundType.STONE)));

    public static final DeferredHolder<Block, Block> SWAMPWEED = BLOCKS.register("swampweed", () -> new SwampweedBlock(
            BlockBehaviour.Properties.of()
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .randomTicks()
    ));
}