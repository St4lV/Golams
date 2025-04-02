package fr.st4lv.golams.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import fr.st4lv.golams.Golams;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class GolamRenderer extends MobRenderer<GolamEntity, GolamModel<GolamEntity>> {

    private final BlacksmithGolamModel blacksmithModel;
    private final CartographerGolamModel cartographerModel;
    private final DelivererGolamModel delivererModel;
    private final GuardGolamModel guardModel;
    private final HarvesterGolamModel harvesterModel;
    private final GolamModel defaultModel;

    public GolamRenderer(EntityRendererProvider.Context context) {
        super(context, new GolamModel<>(context.bakeLayer(GolamModel.LAYER_LOCATION)), 0.25f);

        this.blacksmithModel = new BlacksmithGolamModel(context.bakeLayer(BlacksmithGolamModel.LAYER_LOCATION));
        this.cartographerModel = new CartographerGolamModel(context.bakeLayer(CartographerGolamModel.LAYER_LOCATION));
        this.delivererModel = new DelivererGolamModel(context.bakeLayer(DelivererGolamModel.LAYER_LOCATION));
        this.guardModel = new GuardGolamModel(context.bakeLayer(GuardGolamModel.LAYER_LOCATION));
        this.harvesterModel = new HarvesterGolamModel(context.bakeLayer(HarvesterGolamModel.LAYER_LOCATION));
        this.defaultModel = new GolamModel(context.bakeLayer(GolamModel.LAYER_LOCATION));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(GolamEntity entity) {
        String golam_profession = entity.getTypeVariant().getProfessionName();
        return  ResourceLocation.fromNamespaceAndPath((Golams.MODID), "textures/entity/"+golam_profession+"_golam.png");
    }

    @Override
    public void render(GolamEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        this.model = switch (entity.getTypeVariant()) {
            case BLACKSMITH -> blacksmithModel;
            case CARTOGRAPHER -> cartographerModel;
            case DELIVERER -> delivererModel;
            case HARVESTER -> harvesterModel;
            case GUARD -> guardModel;

            default -> defaultModel;
        };

        poseStack.scale(1f, 1f, 1f);
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}