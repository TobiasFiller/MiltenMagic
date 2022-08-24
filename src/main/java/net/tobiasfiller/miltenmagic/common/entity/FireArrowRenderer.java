package net.tobiasfiller.miltenmagic.common.entity;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.tobiasfiller.miltenmagic.MiltenMagic;

public class FireArrowRenderer extends ArrowRenderer<FireArrowEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(MiltenMagic.MOD_ID, "textures/entity/fire_arrow.png");

    public FireArrowRenderer(EntityRendererProvider.Context manager) {
        super(manager);
    }

    public ResourceLocation getTextureLocation(FireArrowEntity arrow) {
        return TEXTURE;
    }
}