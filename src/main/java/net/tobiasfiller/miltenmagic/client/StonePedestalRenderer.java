package net.tobiasfiller.miltenmagic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.tobiasfiller.miltenmagic.MiltenMagic;
import net.tobiasfiller.miltenmagic.common.blockentity.StonePedestalBlockEntity;

@OnlyIn(Dist.CLIENT)
public class StonePedestalRenderer implements BlockEntityRenderer<StonePedestalBlockEntity> {
    public final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public StonePedestalRenderer(BlockEntityRendererProvider.Context pContext){
    }

    @Override
    public void render(StonePedestalBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        ItemStack stack = new ItemStack(pBlockEntity.getItem());

        if (!stack.isEmpty()){
            pPoseStack.pushPose();
            pPoseStack.translate(0.5D, 1.3225D, 0.5D);
            pPoseStack.scale(0.6f,0.6f,0.6f);
            float f3 = pBlockEntity.getSpin(pPartialTick);
            pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f3));
            itemRenderer.renderStatic(Minecraft.getInstance().player,stack, ItemTransforms.TransformType.FIXED,false,pPoseStack,pBufferSource,Minecraft.getInstance().level,pPackedLight,pPackedOverlay,0);
            pPoseStack.popPose();
        }
    }
}
