package com.agent.sbwnpcaddon.entity.client;

import com.agent.sbwnpcaddon.entity.IvNpcEntity;
import net.minecraft.client.Minecraft;
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
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class IvNpcRenderer extends EntityRenderer<IvNpcEntity> {
    private final ResourceLocation objModelLoc;
    
    private final List<Vector3f> vertices = new ArrayList<>();
    private final List<Vector2f> uvs = new ArrayList<>();
    private final List<Vector3f> normals = new ArrayList<>();
    private final List<int[][]> faces = new ArrayList<>();

    public IvNpcRenderer(EntityRendererProvider.Context renderManager, String modelName) {
        super(renderManager);
        this.objModelLoc = new ResourceLocation("sbw_npc_addon", "models/obj/" + modelName + ".obj");
        parseMultiPartModel(this.objModelLoc);
    }

    private void parseMultiPartModel(ResourceLocation location) {
        try {
            var resourceOpt = Minecraft.getInstance().getResourceManager().getResource(location);
            if (resourceOpt.isPresent()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceOpt.get().open()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) continue;
                        
                        String[] parts = line.split("\\s+");
                        if (parts[0].equals("v")) {
                            vertices.add(new Vector3f(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])));
                        } else if (parts[0].equals("vt")) {
                            uvs.add(new Vector2f(Float.parseFloat(parts[1]), 1.0f - Float.parseFloat(parts[2])));
                        } else if (parts[0].equals("vn")) {
                            normals.add(new Vector3f(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])));
                        } else if (parts[0].equals("f")) {
                            int[][] face = new int[parts.length - 1][3];
                            for (int i = 1; i < parts.length; i++) {
                                String[] indices = parts[i].split("/");
                                face[i - 1][0] = Integer.parseInt(indices[0]) - 1;
                                face[i - 1][1] = indices.length > 1 && !indices[1].isEmpty() ? Integer.parseInt(indices[1]) - 1 : -1;
                                face[i - 1][2] = indices.length > 2 && !indices[2].isEmpty() ? Integer.parseInt(indices[2]) - 1 : -1;
                            }
                            faces.add(face);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(IvNpcEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        
        poseStack.pushPose();
        
        // Typical Minecraft scaling for OBJ models
        // Immersive Vehicles models often need rotation and scaling to match Minecraft coordinates
        poseStack.translate(0.0D, 0.0D, 0.0D);
        poseStack.scale(1.0F, 1.0F, 1.0F); 
        
        // Entity rotation
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-entityYaw + 180));
        
        ResourceLocation texture = getTextureLocation(entity);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        
        renderParts(pose, normal, vertexConsumer, packedLight);
        
        poseStack.popPose();
    }
    
    private void renderParts(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, int light) {
        for (int[][] face : faces) {
            if (face.length == 3) {
                renderVertex(face[0], pose, normal, consumer, light);
                renderVertex(face[1], pose, normal, consumer, light);
                renderVertex(face[2], pose, normal, consumer, light);
                renderVertex(face[2], pose, normal, consumer, light); // Pad to quad
            } else if (face.length == 4) {
                renderVertex(face[0], pose, normal, consumer, light);
                renderVertex(face[1], pose, normal, consumer, light);
                renderVertex(face[2], pose, normal, consumer, light);
                renderVertex(face[3], pose, normal, consumer, light);
            } else if (face.length > 4) {
                // Triangulate n-gons into padded quads (triangle fans)
                for (int i = 1; i < face.length - 1; i++) {
                    renderVertex(face[0], pose, normal, consumer, light);
                    renderVertex(face[i], pose, normal, consumer, light);
                    renderVertex(face[i + 1], pose, normal, consumer, light);
                    renderVertex(face[i + 1], pose, normal, consumer, light); // Pad
                }
            }
        }
    }

    private void renderVertex(int[] indices, Matrix4f pose, Matrix3f normal, VertexConsumer consumer, int light) {
        if (indices[0] < 0 || indices[0] >= vertices.size()) return;
        Vector3f v = vertices.get(indices[0]);
        Vector2f uv = (indices[1] >= 0 && indices[1] < uvs.size()) ? uvs.get(indices[1]) : new Vector2f(0, 0);
        Vector3f n = (indices[2] >= 0 && indices[2] < normals.size()) ? normals.get(indices[2]) : new Vector3f(0, 1, 0);

        consumer.vertex(pose, v.x, v.y, v.z)
                .color(255, 255, 255, 255)
                .uv(uv.x, uv.y)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, n.x, n.y, n.z)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(IvNpcEntity entity) {
        return new ResourceLocation("sbw_npc_addon", "textures/entity/" + entity.getModelName() + ".png");
    }
}
