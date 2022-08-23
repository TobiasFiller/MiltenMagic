package net.tobiasfiller.miltenmagic.core.registry;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.block.MagicalLecternBlock;
import net.tobiasfiller.miltenmagic.common.block.SwampweedBlock;
import net.tobiasfiller.miltenmagic.common.block.TeleportationPlatformBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = MiltenMagic.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BlockRegistry {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MiltenMagic.MOD_ID);

    public static final RegistryObject<Block> TELEPORTATION_PLATFORM_BLOCK =
            BLOCKS.register("teleportation_platform", TeleportationPlatformBlock::new);

    public static final RegistryObject<Block> MAGICAL_LECTERN =
            BLOCKS.register("magical_lectern", MagicalLecternBlock::new);

    public static final RegistryObject<Block> SWAMPWEED = BLOCKS.register("swampweed", () -> new SwampweedBlock(
            BlockBehaviour.Properties.of(Material.PLANT)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .randomTicks()
    ));

    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event){
        ItemBlockRenderTypes.setRenderLayer(SWAMPWEED.get(), RenderType.cutout());
    }
}
