package com.agent.sbwnpcaddon.entity.client;

import com.agent.sbwnpcaddon.entity.SbwNpcEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SbwNpcRenderer extends GeoEntityRenderer<SbwNpcEntity> {
    public SbwNpcRenderer(EntityRendererProvider.Context renderManager, String modelName) {
        super(renderManager, new SbwNpcModel(modelName));
    }

    @Override
    public net.minecraft.client.renderer.RenderType getRenderType(SbwNpcEntity animatable, net.minecraft.resources.ResourceLocation texture, net.minecraft.client.renderer.MultiBufferSource bufferSource, float partialTick) {
        return net.minecraft.client.renderer.RenderType.entityTranslucent(texture);
    }
}
