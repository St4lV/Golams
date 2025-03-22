package fr.st4lv.golams.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.st4lv.golams.Golams;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;


public class CartographerGolamModel extends GolamModel<GolamEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(Golams.MODID, "cartographer_golam"), "main"
	);

	private final ModelPart body;
	private final ModelPart head;


	public CartographerGolamModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.head = this.body.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -11.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition leg_r = body.addOrReplaceChild("leg_r", CubeListBuilder.create().texOffs(24, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 2.0F));

		PartDefinition leg_l = body.addOrReplaceChild("leg_l", CubeListBuilder.create().texOffs(16, 24).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -2.0F));

		PartDefinition arm_r = body.addOrReplaceChild("arm_r", CubeListBuilder.create().texOffs(8, 23).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, 4.0F));

		PartDefinition arm_l = body.addOrReplaceChild("arm_l", CubeListBuilder.create().texOffs(0, 23).addBox(-1.0F, 0.0F, -2.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -10.0F, -4.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(20, 16).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(16, 30).addBox(-1.0F, -4.0F, 2.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(16, 30).addBox(-1.0F, -4.0F, -3.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 16).addBox(-2.0F, -5.0F, -3.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(GolamEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.applyHeadRotation(netHeadYaw, headPitch);

		this.animateWalk(GolamAnimations.walk, limbSwing, limbSwingAmount, 2f, 2.5f);
		this.animate(GolamEntity.idleAnimationState, GolamAnimations.idle, ageInTicks, 1f);

	}

	private void applyHeadRotation(float headYaw, float headPitch) {
		headYaw = Mth.clamp(headYaw, -30f, 30f);
		headPitch = Mth.clamp(headPitch, -5f, 5);

		this.head.yRot = headYaw * ((float)Math.PI / 180f);
		this.head.xRot = headPitch *  ((float)Math.PI / 180f);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
	}

	@Override
	public ModelPart root() {
		return body;
	}
}
