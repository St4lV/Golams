package fr.st4lv.golams.entity.golam_goals.blacksmith;

import fr.st4lv.golams.entity.GolamProfessions;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class HealOtherGolamsGoal extends Goal {
    private final GolamEntity blacksmith;
    private GolamEntity targetGolam;
    private final double speed;
    private final float searchRadius;
    private int heal_cd = 0;
    private int cooldown = 0;

    public HealOtherGolamsGoal(GolamEntity blacksmith, double speed, float searchRadius) {
        this.blacksmith = blacksmith;
        this.speed = speed;
        this.searchRadius = searchRadius;
    }

    @Override
    public boolean canUse() {
        if (blacksmith.getTypeVariant() != GolamProfessions.BLACKSMITH || cooldown > 0) {
            cooldown--;
            return false;
        }

        List<GolamEntity> golams = blacksmith.level().getEntitiesOfClass(GolamEntity.class,
                blacksmith.getBoundingBox().inflate(searchRadius),
                e -> e != blacksmith && e.getHealth() < e.getMaxHealth());

        for (GolamEntity potentialTarget : golams) {
            if (hasSmoothBasalt()) {
                targetGolam = potentialTarget;
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {
        if (targetGolam != null) {
            moveTowardsTarget();
        }
    }

    @Override
    public boolean canContinueToUse() {
        return targetGolam != null && targetGolam.isAlive() &&
                targetGolam.getHealth() < targetGolam.getMaxHealth() &&
                hasSmoothBasalt();
    }

    @Override
    public void tick() {
        if (targetGolam == null) return;

        double distanceSqr = blacksmith.distanceToSqr(targetGolam.getX(), targetGolam.getY(), targetGolam.getZ());

        if (distanceSqr > 4.0) {
            moveTowardsTarget();
        } else {
            if (heal_cd > 0) {
                heal_cd--;
            }

            if (targetGolam != null && targetGolam.isAlive() && targetGolam.getHealth() < targetGolam.getMaxHealth()) {
                healTargetGolam();
                if (targetGolam.getHealth() == targetGolam.getMaxHealth()){
                    targetGolam=null;
                    cooldown=50;
                }
            }
        }
    }

    private void moveTowardsTarget() {
        if (targetGolam != null) {
            blacksmith.getNavigation().moveTo(
                    targetGolam.getX() + 0.5,
                    targetGolam.getY(),
                    targetGolam.getZ() + 0.5,
                    speed
            );
        }
    }

    private boolean hasSmoothBasalt() {
        for (ItemStack item : blacksmith.getInventory().getItems()) {
            if (item.getItem() == Items.SMOOTH_BASALT) {
                return true;
            }
        }
        blacksmith.requestRestock();
        return false;
    }

    private void healTargetGolam() {
        if (hasSmoothBasalt() && heal_cd == 0) {
            targetGolam.heal(GolamEntity.repair_value_by_smooth_basalt);
            removeSmoothBasalt();
            blacksmith.playSound(SoundEvents.WOLF_ARMOR_REPAIR, 0.2F, 1.0F);
            blacksmith.playSound(SoundEvents.BASALT_BREAK, 0.5F, 1.0F);
            blacksmith.swing(InteractionHand.MAIN_HAND);
            heal_cd =5;
        }
    }

    private void removeSmoothBasalt() {
        for (int i = 0; i < blacksmith.getInventory().getContainerSize(); ++i) {
            ItemStack item = blacksmith.getInventory().getItem(i);
            if (item.getItem() == Items.SMOOTH_BASALT) {
                item.shrink(1);
                break;
            }
        }
    }
}

