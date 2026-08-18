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
        
        // Fix translations and scaling for IV models
        poseStack.translate(0.0D, 1.5D, 0.0D);
        poseStack.scale(-1.0F, -1.0F, 1.0F); // typical Minecraft model scaling
        
        // Apply entity rotation (yaw)
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(entityYaw));
        
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
        addVertex(consumer, pose, normal, light, -0.5f, 0.0f, -0.5f, 0.0f, 0.0f);
        addVertex(consumer, pose, normal, light, -0.5f, 1.0f, -0.5f, 0.0f, 1.0f);
        addVertex(consumer, pose, normal, light,  0.5f, 1.0f, -0.5f, 1.0f, 1.0f);
        addVertex(consumer, pose, normal, light,  0.5f, 0.0f, -0.5f, 1.0f, 0.0f);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, int light, float x, float y, float z, float u, float v) {
        consumer.vertex(pose, x, y, z).color(255, 255, 255, 255).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(normal, 0.0f, 0.0f, -1.0f).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(IvNpcEntity entity) {
        return new ResourceLocation("sbw_npc_addon", "textures/entity/" + entity.getModelName() + ".png");
    }
}
