package fr.st4lv.golams.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.st4lv.golams.block.custom.GolamInterface;
import fr.st4lv.golams.block.entity.GolamInterfaceBE;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GolamInterfaceBERenderer implements BlockEntityRenderer<GolamInterfaceBE> {

    public GolamInterfaceBERenderer(BlockEntityRendererProvider.Context context) {

    }
    @Override
    public void render(GolamInterfaceBE blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack stack =  blockEntity.inventory.getStackInSlot(0);

        if (!stack.isEmpty()) {
            poseStack.pushPose();

            Direction direction = blockEntity.getBlockState().getValue(GolamInterface.FACING);

            switch (direction) {
                case UP -> {
                    poseStack.translate(0.5, 0.85, 0.5);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(90));

                }
                case DOWN -> {
                    poseStack.translate(0.5, 0.15, 0.5);
                    poseStack.mulPose(Axis.XP.rotationDegrees(90));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(90));

                }
                case NORTH -> {
                    poseStack.translate(0.5, 0.5, 0.15);
                    poseStack.mulPose(Axis.YP.rotationDegrees(180));
                }
                case SOUTH -> poseStack.translate(0.5, 0.5, 0.85);
                case WEST -> {
                    poseStack.translate(0.15, 0.5, 0.5);
                    poseStack.mulPose(Axis.YP.rotationDegrees(-90));

                }
                case EAST -> {
                    poseStack.translate(0.85, 0.5, 0.5);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));

                }
            }
            poseStack.scale(0.5f, 0.5f, 0.5f);

            Minecraft.getInstance().getItemRenderer().renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    getLightLevel(Objects.requireNonNull(blockEntity.getLevel()),
                            blockEntity.getBlockPos()),
                    packedOverlay,
                    poseStack,
                    bufferSource,
                    blockEntity.getLevel(),
                    0
            );

            poseStack.popPose();
        }
}
    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}
