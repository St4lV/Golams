package fr.st4lv.golams.entity.golam_goals;

import fr.st4lv.golams.block.entity.GolamInterfaceBE;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class RestockToolGoal extends Goal {
    private final GolamEntity entity;
    private final double speed;
    private BlockPos targetBlock;
    private int cooldown;

    public RestockToolGoal(GolamEntity entity, double speed) {
        this.entity = entity;
        this.speed = speed;
        this.cooldown = 0;
    }

    @Override
    public boolean canUse() {
        if (!entity.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        targetBlock = entity.findAssignedItemGolamInterface(entity.assignedTool);
        cooldown = 80;
        return targetBlock != null;
    }

    @Override
    public void start() {
        if (targetBlock != null) {
            entity.getNavigation().moveTo(
                    targetBlock.getX() + 0.5,
                    targetBlock.getY(),
                    targetBlock.getZ() + 0.5,
                    speed
            );
        }
    }

    @Override
    public boolean canContinueToUse() {
        return targetBlock != null && entity.distanceToSqr(Vec3.atCenterOf(targetBlock)) > 1.0;
    }

    @Override
    public void tick() {
        if (targetBlock == null || !entity.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) return;
        entity.getNavigation().moveTo(
                targetBlock.getX() + 0.5,
                targetBlock.getY(),
                targetBlock.getZ() + 0.5,
                speed
        );

        BlockEntity be = entity.level().getBlockEntity(targetBlock);
        if (!(be instanceof GolamInterfaceBE interfaceBE)) return;
        Container chest = interfaceBE.getLinkedChest();
        if (chest instanceof ChestBlockEntity chestEntity) chest = getChestContainer(chestEntity);
        if (chest == null || chest.isEmpty()) return;

        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == entity.assignedTool) {
                entity.setItemSlot(EquipmentSlot.MAINHAND, stack.split(1));
                break;
            }
        }
        cooldown = 80;
    }

    public static Container getChestContainer(ChestBlockEntity chestEntity) {
        Level level = chestEntity.getLevel();
        BlockState state = chestEntity.getBlockState();
        if (state.getBlock() instanceof ChestBlock chestBlock) {
            assert level != null;
            return ChestBlock.getContainer(chestBlock, state, level, chestEntity.getBlockPos(), true);
        }
        return chestEntity;
    }

    @Override
    public void stop() {
        targetBlock = null;
    }
}