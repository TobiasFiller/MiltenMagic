package net.tobiasfiller.miltenmagic.core.event;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.entity.FireArrowRenderer;
import net.tobiasfiller.miltenmagic.core.registry.EntityRegistry;

import static net.tobiasfiller.miltenmagic.core.registry.BlockRegistry.SWAMPWEED;

@Mod.EventBusSubscriber(modid = MiltenMagic.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event){
        EntityRenderers.register(EntityRegistry.FIRE_ARROW.get(), FireArrowRenderer::new);
        ItemBlockRenderTypes.setRenderLayer(SWAMPWEED.get(), RenderType.cutout());
    }
}

