package com.agent.sbwnpcaddon.entity.client;

import com.agent.sbwnpcaddon.entity.IvNpcEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class IvNpcRenderer extends EntityRenderer<IvNpcEntity> {
    public IvNpcRenderer(EntityRendererProvider.Context renderManager, String modelName) {
        super(renderManager);
    }

    @Override
    public ResourceLocation getTextureLocation(IvNpcEntity entity) {
        return new ResourceLocation("sbw_npc_addon", "textures/entity/" + entity.getModelName() + ".png");
    }
}
