package fr.st4lv.golams.entity.golam_goals.blacksmith;

import fr.st4lv.golams.block.entity.GolamInterfaceBE;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class RestockSmoothBasaltGoal extends Goal {
    private final GolamEntity blacksmith;
    private final double speed;
    private BlockPos targetAssignedBlock;
    private final Item smooth_basalt = Items.SMOOTH_BASALT;
    private int cooldown;

    public RestockSmoothBasaltGoal(GolamEntity blacksmith, double speed) {
        this.blacksmith = blacksmith;
        this.speed = speed;
        this.cooldown = 0;
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        ItemStack slot0 = blacksmith.getInventory().getItem(0);
        if (slot0.getItem() == smooth_basalt && slot0.getCount() >= 16) {
            blacksmith.resetRestockFlag();
            return false;
        }

        targetAssignedBlock = blacksmith.findAssignedItemGolamInterface(smooth_basalt);

        if (targetAssignedBlock != null) {
            blacksmith.resetRestockFlag();
            return true;
        }
        return blacksmith.needsRestocking();
    }


    @Override
    public void start() {
        if (targetAssignedBlock != null) {
            blacksmith.getNavigation().moveTo(
                    targetAssignedBlock.getX() + 0.5,
                    targetAssignedBlock.getY(),
                    targetAssignedBlock.getZ() + 0.5,
                    speed
            );
        }
    }

    @Override
    public void stop() {
        targetAssignedBlock = null;
        cooldown = 200;
    }

    @Override
    public boolean canContinueToUse() {
        return targetAssignedBlock != null &&
               blacksmith.distanceToSqr(Vec3.atCenterOf(targetAssignedBlock)) > 1.0;
    }

    @Override
    public void tick() {
        if (targetAssignedBlock == null) return;

        double distanceSqr = blacksmith.distanceToSqr(Vec3.atCenterOf(targetAssignedBlock));

        if (distanceSqr > 4.0) {
            blacksmith.getNavigation().moveTo(
                    targetAssignedBlock.getX() + 0.5,
                    targetAssignedBlock.getY(),
                    targetAssignedBlock.getZ() + 0.5,
                    speed
            );
            return;
        }

        BlockEntity be = blacksmith.level().getBlockEntity(targetAssignedBlock);
        if (be instanceof GolamInterfaceBE interfaceBE) {
            Container chest = interfaceBE.getLinkedChest();
            if (chest != null) {
                int amountNeeded = 64 - blacksmith.getInventory().getItem(0).getCount();
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    ItemStack stack = chest.getItem(i);
                    if (stack.getItem() == smooth_basalt) {
                        int toTake = Math.min(stack.getCount(), amountNeeded);
                        blacksmith.getInventory().setItem(0, stack.copyWithCount(toTake));
                        stack.shrink(toTake);
                        break;
                    }
                }
            }
        }

        cooldown = 200;
        targetAssignedBlock = null;
    }

}


