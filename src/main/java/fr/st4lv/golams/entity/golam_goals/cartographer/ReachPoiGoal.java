package fr.st4lv.golams.entity.golam_goals.cartographer;

import fr.st4lv.golams.block.entity.GolamInterfaceBE;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class ReachPoiGoal extends Goal {

    private final GolamEntity entity;
    private final double speed;
    private int cooldown = 0;
    private BlockPos targetBlock;

    public ReachPoiGoal(GolamEntity entity, double speed) {
        this.entity = entity;
        this.speed = speed;
    }

    @Override
    public boolean canUse() {
        if (!entity.shouldReachPoi()) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        targetBlock = findAssignedItemGolamInterface(Items.FILLED_MAP);
        cooldown = 100;
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
        return targetBlock != null &&
                entity.distanceToSqr(Vec3.atCenterOf(targetBlock)) > 0.5;
    }

    public BlockPos findAssignedItemGolamInterface(Item item) {
        for (GolamEntity.AssignedBlock ab : entity.getAssignedBlocks()) {
            if (ab.getItem() == item) {
                BlockPos pos = ab.getBlockPos();
                        return pos;
                    }
        }
        return null;
    }

    @Override
    public void tick() {
        if (entity.distanceToSqr(Vec3.atCenterOf(targetBlock)) < 0.9) {
            entity.resetReachPoi();
            return;
        }
        if (targetBlock != null && cooldown>0) {
            Vec3 targetPos = Vec3.atCenterOf(targetBlock);

            entity.getLookControl().setLookAt(targetPos.x, targetPos.y, targetPos.z);
        } else entity.resetReachPoi();
    }
}

