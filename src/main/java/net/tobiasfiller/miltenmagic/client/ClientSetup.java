package net.tobiasfiller.miltenmagic.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.entity.FireArrowRenderer;
import net.tobiasfiller.miltenmagic.core.registry.BlockEntityRegistry;
import net.tobiasfiller.miltenmagic.core.registry.BlockRegistry;
import net.tobiasfiller.miltenmagic.core.registry.EntityRegistry;
import net.tobiasfiller.miltenmagic.core.registry.ItemRegistry;

@EventBusSubscriber(modid = MiltenMagic.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    /**
     * Colour of the "T" while the item is undyed. The overlay textures are bright silhouettes
     * of the glyph (layer1, tint index 1), so the tint alone decides the visible colour.
     */
    private static final int SCROLL_GLYPH_COLOR = 0xFF5A5022;
    private static final int RUNE_GLYPH_COLOR = 0xFF2A3175;

    @SubscribeEvent
    public static void onClientSetupEvent(FMLClientSetupEvent event){
        EntityRenderers.register(EntityRegistry.FIRE_ARROW.get(), FireArrowRenderer::new);
        ItemBlockRenderTypes.setRenderLayer(BlockRegistry.SWAMPWEED.get(), RenderType.cutout());
        BlockEntityRenderers.register(BlockEntityRegistry.MAGICAL_LECTERN_BLOCK_ENTITY.get(), MagicalLecternRenderer::new);
        BlockEntityRenderers.register(BlockEntityRegistry.STONE_PEDESTAL_BLOCK_ENTITY.get(), StonePedestalRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(ClientSetup::getTeleportGlyphColor,
                ItemRegistry.TELEPORTATION_SCROLL.get(), ItemRegistry.TELEPORTATION_RUNE.get());
    }

    /**
     * Tints only the glyph layer. Index 0 is the paper/stone base texture (see
     * {@code ItemModelGenerator}, which uses the layer number as tint index) and must stay
     * untinted, hence -1 there.
     */
    private static int getTeleportGlyphColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 1) {
            return -1;
        }
        int fallback = stack.is(ItemRegistry.TELEPORTATION_RUNE.get())
                ? RUNE_GLYPH_COLOR
                : SCROLL_GLYPH_COLOR;
        return DyedItemColor.getOrDefault(stack, fallback);
    }
}
