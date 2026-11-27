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
 * APG door bottom model for Tianjin variant.
 * Re-created from MTR 3.5.9 RenderPSDAPGDoor.ModelAPGDoorBottom since it is private.
 */
public class ModelAPGDoorBottom extends EntityModel<Entity> {
    private final ModelPart root;

    public ModelAPGDoorBottom() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition rootDef = mesh.getRoot();

        PartDefinition bone = rootDef.addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-8, -16, -7, 16, 16, 1)
                        .texOffs(0, 17).addBox(-8, -6, -8, 16, 6, 1),
                PartPose.ZERO);

        bone.addOrReplaceChild("cube_r1", CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-8, -2, 0, 16, 2, 1),
                PartPose.offsetAndRotation(0, -6, -8, -0.7854F, 0, 0));

        this.root = LayerDefinition.create(mesh, 34, 27).bakeRoot();
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
