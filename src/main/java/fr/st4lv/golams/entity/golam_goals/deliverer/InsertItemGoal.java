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

public class InsertItemGoal extends Goal {
    private final GolamEntity entity;
    private final double speed;
    private BlockPos targetBlock;
    private int cooldown = 0;
    private int deposit_item_cd=0;

    public InsertItemGoal(GolamEntity entity, double speed) {
        this.entity = entity;
        this.speed = speed;
    }

    @Override
    public boolean canUse() {
        if (entity.getInventory().getItem(0).isEmpty()) {
            return false;
        }
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        ItemStack held = entity.getInventory().getItem(0);

        if (held.getItem()==Items.AIR) {
            return false;
        }
        targetBlock = entity.findAssignedItemGolamInterface(held.getItem());

        cooldown=100;
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
        if (targetBlock == null) return;
        boolean deposited = false;

        if (deposit_item_cd > 0) {
            deposit_item_cd--;
            return;
        }

        double distanceSqr = entity.distanceToSqr(Vec3.atCenterOf(targetBlock));
        if (distanceSqr > 4.0) {
            entity.getNavigation().moveTo(
                    targetBlock.getX() + 0.5,
                    targetBlock.getY(),
                    targetBlock.getZ() + 0.5,
                    speed
            );
            return;
        }

        BlockEntity be = entity.level().getBlockEntity(targetBlock);
        if (!(be instanceof GolamInterfaceBE interfaceBE)) return;

        Container chest = interfaceBE.getLinkedChest();
        if (chest == null) return;

        if (chest instanceof ChestBlockEntity chestEntity)
            chest = getChestContainer(chestEntity);

        ItemStack held = entity.getInventory().getItem(0);
        if (held.isEmpty() || held.getItem() == Items.AIR)
            return;

        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack slotStack = chest.getItem(i);
            if (slotStack.isEmpty() ||
                    (ItemStack.isSameItem(slotStack, held) && slotStack.getCount() < slotStack.getMaxStackSize())) {
                int maxStack = slotStack.isEmpty() ? held.getMaxStackSize() : slotStack.getMaxStackSize();
                int toDeposit = Math.min(held.getCount(), maxStack - slotStack.getCount());
                chest.setItem(i, held.copyWithCount(slotStack.getCount() + toDeposit));
                held.shrink(toDeposit);
                deposited = true;

                if (entity.getInventory().isEmpty()) {
                    entity.updateGoals();
                    return;
                }
            }
        }

        if (!deposited) {
            deposit_item_cd = 50;
        }
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