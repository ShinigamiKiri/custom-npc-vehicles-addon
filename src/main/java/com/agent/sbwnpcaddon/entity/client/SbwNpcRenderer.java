package com.agent.sbwnpcaddon.entity.client;

import com.agent.sbwnpcaddon.entity.SbwNpcEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SbwNpcRenderer extends GeoEntityRenderer<SbwNpcEntity> {
    public SbwNpcRenderer(EntityRendererProvider.Context renderManager, String modelName) {
        super(renderManager, new SbwNpcModel(modelName));
    }
}
