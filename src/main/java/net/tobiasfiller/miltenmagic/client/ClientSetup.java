package net.tobiasfiller.miltenmagic.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.entity.FireArrowRenderer;
import net.tobiasfiller.miltenmagic.core.registry.BlockEntityRegistry;
import net.tobiasfiller.miltenmagic.core.registry.BlockRegistry;
import net.tobiasfiller.miltenmagic.core.registry.EntityRegistry;


@Mod.EventBusSubscriber(modid = MiltenMagic.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event){
        EntityRenderers.register(EntityRegistry.FIRE_ARROW.get(), FireArrowRenderer::new);
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.SWAMPWEED.get(), RenderType.cutout());
        BlockEntityRenderers.register(BlockEntityRegistry.MAGICAL_LECTERN_BLOCK_ENTITY.get(), MagicalLecternRenderer::new);
        BlockEntityRenderers.register(BlockEntityRegistry.STONE_PEDESTAL_BLOCK_ENTITY.get(), StonePedestalRenderer::new);
    }
}

