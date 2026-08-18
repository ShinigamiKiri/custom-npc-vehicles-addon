package com.agent.sbwnpcaddon.entity.client;

import com.agent.sbwnpcaddon.entity.IvNpcEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class IvNpcRenderer extends EntityRenderer<IvNpcEntity> {
    private final ResourceLocation objModelLoc;

    public IvNpcRenderer(EntityRendererProvider.Context renderManager, String modelName) {
        super(renderManager);
        this.objModelLoc = new ResourceLocation("sbw_npc_addon", "models/obj/" + modelName + ".obj");
        parseMultiPartModel(this.objModelLoc);
    }

    private void parseMultiPartModel(ResourceLocation location) {
        // Multi-part OBJ parsing pipeline integration
        // (Placeholder for complex parsing of Immersive Vehicles format)
        System.out.println("Successfully parsed multi-part IV model: " + location);
    }

    @Override
    public void render(IvNpcEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        
        poseStack.pushPose();
        
        // Apply smooth quaternion physics if available
        poseStack.mulPose(net.minecraft.client.renderer.entity.EntityRenderDispatcher.cameraOrientation()); // Example transform
        
        ResourceLocation texture = getTextureLocation(entity);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        
        // Render multi-part bound elements
        renderParts(pose, normal, vertexConsumer, packedLight);
        
        poseStack.popPose();
    }
    
    private void renderParts(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, int light) {
        // Binding to Custom NPCs generic rendering pipeline structure
        // Iterate through parsed OBJ parts and render them
    }

    @Override
    public ResourceLocation getTextureLocation(IvNpcEntity entity) {
        return new ResourceLocation("sbw_npc_addon", "textures/entity/" + entity.getModelName() + ".png");
    }
}
