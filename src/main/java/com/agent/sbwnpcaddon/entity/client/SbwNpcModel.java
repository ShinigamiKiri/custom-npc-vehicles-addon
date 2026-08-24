package com.agent.sbwnpcaddon.entity.client;

import com.agent.sbwnpcaddon.entity.SbwNpcEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SbwNpcModel extends GeoModel<SbwNpcEntity> {
    private final String modelName;

    public SbwNpcModel(String modelName) {
        this.modelName = modelName;
    }

    @Override
    public ResourceLocation getModelResource(SbwNpcEntity object) {
        return new ResourceLocation("sbw_npc_addon", "geo/" + modelName + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SbwNpcEntity object) {
        return new ResourceLocation("sbw_npc_addon", "textures/entity/" + modelName + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(SbwNpcEntity object) {
        return new ResourceLocation("sbw_npc_addon", "animations/" + modelName + ".animation.json");
    }

    @Override
    public void setCustomAnimations(SbwNpcEntity animatable, long instanceId, software.bernie.geckolib.core.animation.AnimationState<SbwNpcEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        software.bernie.geckolib.cache.object.GeoBone rootBone = this.getAnimationProcessor().getBone("root");
        if (rootBone != null) {
            rootBone.setRotY(rootBone.getRotY() + (float)Math.PI);
        }
    }
}
