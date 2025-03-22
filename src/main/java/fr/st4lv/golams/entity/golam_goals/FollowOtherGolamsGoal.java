package fr.st4lv.golams.entity.golam_goals;

import fr.st4lv.golams.entity.GolamProfessions;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.List;

public class FollowOtherGolamsGoal extends Goal {
    private final GolamEntity entity;
    private GolamEntity targetGolam;
    private final double speed;
    private final float followDistance;

    public FollowOtherGolamsGoal(GolamEntity entity, double speed, float followDistance) {
        this.entity = entity;
        this.speed = speed;
        this.followDistance = followDistance;
    }

    @Override
    public boolean canUse() {
        List<GolamEntity> golams = entity.level().getEntitiesOfClass(GolamEntity.class, entity.getBoundingBox().inflate(followDistance),
                e -> e.getTypeVariant() != GolamProfessions.GUARD && entity.distanceTo(e) > 10.0);

        if (!golams.isEmpty()) {
            targetGolam = golams.get(0);
            return true;
        }
        return false;
    }


    @Override
    public void start() {
        if (targetGolam != null) {
            entity.getNavigation().moveTo(targetGolam, speed);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return targetGolam != null && targetGolam.isAlive() && entity.distanceTo(targetGolam) > 5.0;
    }
}
