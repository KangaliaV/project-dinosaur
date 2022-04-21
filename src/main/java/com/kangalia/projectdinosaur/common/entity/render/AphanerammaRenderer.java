package com.kangalia.projectdinosaur.common.entity.render;

import com.kangalia.projectdinosaur.common.entity.AphanerammaEntity;
import com.kangalia.projectdinosaur.common.entity.model.AphanerammaModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class AphanerammaRenderer extends GeoEntityRenderer<AphanerammaEntity> {

    public AphanerammaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AphanerammaModel());
        this.shadowRadius = 0.6F;
    }

    @Override
    public void render(GeoModel model, AphanerammaEntity animatable, float partialTicks, RenderType type, PoseStack matrixStackIn, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if (animatable.isBaby()) {
            matrixStackIn.scale(0.4F, 0.4F, 0.4F);
        }
        super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
