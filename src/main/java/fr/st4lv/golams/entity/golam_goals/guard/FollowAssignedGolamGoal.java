package fr.st4lv.golams.entity.golam_goals.guard;

import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.List;
import java.util.UUID;

public class FollowAssignedGolamGoal extends Goal {
    private final GolamEntity entity;
    private GolamEntity targetGolam;
    private final double speed;
    private int cooldown=0;

    public FollowAssignedGolamGoal(GolamEntity entity, double speed) {
        this.entity = entity;
        this.speed = speed;
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        List<GolamEntity.AssignedGolams> golamList = entity.getAssignedGolams();
        if (!golamList.isEmpty()) {
            UUID uuid = golamList.getLast().getGolamUuid();
            targetGolam = findGolamByUUID(uuid);
            return targetGolam != null;
        }
        return false;
    }

    private GolamEntity findGolamByUUID(UUID uuid) {
        return entity.level().getEntitiesOfClass(GolamEntity.class, entity.getBoundingBox().inflate(50))
                .stream()
                .filter(g -> g.getUUID().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void start() {
        if (targetGolam != null) {
            entity.getNavigation().moveTo(targetGolam, speed);
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (entity.distanceTo(targetGolam) < 5.0) {
            cooldown=20;
            return false;
        }
        return targetGolam != null && targetGolam.isAlive() && entity.distanceTo(targetGolam) > 5.0;
    }
}
