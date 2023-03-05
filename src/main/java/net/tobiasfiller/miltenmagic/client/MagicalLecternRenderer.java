package net.tobiasfiller.miltenmagic.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.tobiasfiller.miltenmagic.common.blockentity.MagicalLecternBlockEntity;

public class MagicalLecternRenderer implements BlockEntityRenderer<MagicalLecternBlockEntity> {
    private final BookModel bookModel;

    public MagicalLecternRenderer(BlockEntityRendererProvider.Context pContext) {
        this.bookModel = new BookModel(pContext.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(MagicalLecternBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        BlockState blockstate = pBlockEntity.getBlockState();

        pPoseStack.pushPose();
        pPoseStack.translate(0.5D, 1.0625D, 0.5D);
        pPoseStack.scale(0.95f,0.95f,0.95f);
        float f = blockstate.getValue(LecternBlock.FACING).getClockWise().toYRot();
        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(-f));
        pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(67.5F));
        pPoseStack.translate(0.215D, 0.01D, 0.0D);
        this.bookModel.setupAnim(0.0F, 0.1F, 0.9F, 1.2F);
        VertexConsumer vertexconsumer = EnchantTableRenderer.BOOK_LOCATION.buffer(pBufferSource, RenderType::entitySolid);
        this.bookModel.render(pPoseStack, vertexconsumer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        pPoseStack.popPose();
    }
}
