package younin.use.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

/**
 * Simple cube model for Tianjin door rendering.
 * Ported from MTR 4.0 ModelSingleCube to MTR 3.5.9.
 */
public class ModelSingleCube extends EntityModel<Entity> {
    private final ModelPart root;

    public ModelSingleCube() {
        this(1, 1, 0, 0, 0, 1, 1, 1);
    }

    public ModelSingleCube(int textureWidth, int textureHeight, float x, float y, float z, float length, float height, float depth) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootDef = mesh.getRoot();
        rootDef.addOrReplaceChild("cube", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(x - 8, y - 16, z - 8, length, height, depth),
            PartPose.ZERO);
        this.root = LayerDefinition.create(mesh, textureWidth, textureHeight).bakeRoot();
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
