package fr.st4lv.golams.entity.golam_goals.deliverer;

import fr.st4lv.golams.block.entity.GolamInterfaceBE;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ExportItemGoal extends Goal {
    private final GolamEntity entity;
    private final double speed;
    private BlockPos targetBlock;
    private int cooldown;

    public ExportItemGoal(GolamEntity entity, double speed) {
        this.entity = entity;
        this.speed = speed;
        this.cooldown = 0;
    }

    @Override
    public boolean canUse() {
        if (!entity.shouldExport()) return false;
        if (!entity.getInventory().getItem(0).isEmpty()) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        targetBlock = entity.findAssignedItemGolamInterface(Items.AIR);
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
        return targetBlock != null &&
                entity.distanceToSqr(Vec3.atCenterOf(targetBlock)) > 1.0;
    }

    @Override
    public void tick() {
        if (!entity.shouldExport()){
            entity.resetExportFlag();
            cooldown = 80;
            return;
        }
        if (targetBlock == null) return;
        if (!entity.getInventory().isEmpty()) {
            return;
        }
        double distanceSqr = entity.distanceToSqr(Vec3.atCenterOf(targetBlock));
        if (distanceSqr > 4.0) {
            entity.getNavigation().moveTo(
                    targetBlock.getX() + 0.5,
                    targetBlock.getY(),
                    targetBlock.getZ() + 0.5,
                    speed
            ); return;
        }

        BlockEntity be = entity.level().getBlockEntity(targetBlock);
        if (!(be instanceof GolamInterfaceBE interfaceBE)) return;
        Container chest = interfaceBE.getLinkedChest();
        if (chest == null) return;
        if (chest instanceof ChestBlockEntity chestEntity) chest = getChestContainer(chestEntity);
        if (chest.isEmpty()) {
            entity.resetExportFlag();
            cooldown = 80;
            return;
        }
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack chestStack = chest.getItem(i);
            if (!chestStack.isEmpty()) {
                if (entity.getInventory().getItem(0).isEmpty()) {
                    int maxStack = chestStack.getMaxStackSize();
                    int toTake = Math.min(chestStack.getCount(), maxStack);
                    ItemStack pickedItem = chestStack.copyWithCount(toTake);
                    entity.getInventory().setItem(0, pickedItem);
                    chestStack.shrink(toTake);
                }
                else if (ItemStack.isSameItem(entity.getInventory().getItem(0), chestStack)) {
                    ItemStack heldStack = entity.getInventory().getItem(0);
                    int maxStack = heldStack.getMaxStackSize();
                    int needed = maxStack - heldStack.getCount();
                    if (needed > 0) {
                        int toTake = Math.min(chestStack.getCount(), needed);
                        heldStack.grow(toTake);
                        chestStack.shrink(toTake);
                    }
                }
                if (!entity.getInventory().getItem(0).isEmpty() &&
                    entity.getInventory().getItem(0).getCount() >= entity.getInventory().getItem(0).getMaxStackSize())
                    break;
            }
        }
        entity.resetExportFlag();
        cooldown = 80;
    }

    public static Container getChestContainer(ChestBlockEntity chestEntity) {
        Level level = chestEntity.getLevel();
        BlockState state = chestEntity.getBlockState();

        if (state.getBlock() instanceof ChestBlock chestBlock)
            return ChestBlock.getContainer(chestBlock, state, level, chestEntity.getBlockPos(), true);
        return chestEntity;
    }

    @Override
    public void stop() {
        targetBlock = null;
    }
}