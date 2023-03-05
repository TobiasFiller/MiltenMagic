package net.tobiasfiller.miltenmagic.core.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.block.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class BlockRegistry {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MiltenMagic.MOD_ID);

    public static final RegistryObject<Block> TELEPORTATION_PLATFORM_BLOCK =
            BLOCKS.register("teleportation_platform", TeleportationPlatformBlock::new);

    public static final RegistryObject<Block> MAGICAL_LECTERN =
            BLOCKS.register("magical_lectern", MagicalLecternBlock::new);

    // -Tables
    public static final RegistryObject<Block> TABLE =
            BLOCKS.register("table", TableBlock::new);

    public static final RegistryObject<Block> ACACIA_TABLE =
            BLOCKS.register("acacia_table", TableBlock::new);

    public static final RegistryObject<Block> BIRCH_TABLE =
            BLOCKS.register("birch_table", TableBlock::new);

    public static final RegistryObject<Block> CRIMSON_TABLE =
            BLOCKS.register("crimson_table", TableBlock::new);

    public static final RegistryObject<Block> DARK_OAK_TABLE =
            BLOCKS.register("dark_oak_table", TableBlock::new);

    public static final RegistryObject<Block> JUNGLE_TABLE =
            BLOCKS.register("jungle_table", TableBlock::new);

    public static final RegistryObject<Block> OAK_TABLE =
            BLOCKS.register("oak_table", TableBlock::new);

    public static final RegistryObject<Block> SPRUCE_TABLE =
            BLOCKS.register("spruce_table", TableBlock::new);

    public static final RegistryObject<Block> WARPED_TABLE =
            BLOCKS.register("warped_table", TableBlock::new);


    public static final RegistryObject<Block> STONE_PEDESTAL =
            BLOCKS.register("stone_pedestal", StonePedestalBlock::new);

    public static final RegistryObject<Block> SWAMPWEED = BLOCKS.register("swampweed", () -> new SwampweedBlock(
            BlockBehaviour.Properties.of(Material.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .randomTicks()
    ));
}

