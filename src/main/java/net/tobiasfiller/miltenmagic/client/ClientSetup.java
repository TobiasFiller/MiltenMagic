package net.tobiasfiller.miltenmagic.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.entity.FireArrowRenderer;
import net.tobiasfiller.miltenmagic.core.registry.BlockEntityRegistry;
import net.tobiasfiller.miltenmagic.core.registry.BlockRegistry;
import net.tobiasfiller.miltenmagic.core.registry.EntityRegistry;

@EventBusSubscriber(modid = MiltenMagic.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event){
        EntityRenderers.register(EntityRegistry.FIRE_ARROW.get(), FireArrowRenderer::new);
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.SWAMPWEED.get(), RenderType.cutout());
        BlockEntityRenderers.register(BlockEntityRegistry.MAGICAL_LECTERN_BLOCK_ENTITY.get(), MagicalLecternRenderer::new);
        BlockEntityRenderers.register(BlockEntityRegistry.STONE_PEDESTAL_BLOCK_ENTITY.get(), StonePedestalRenderer::new);
    }
}
