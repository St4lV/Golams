package fr.st4lv.golams.entity.golam_goals.deliverer;

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

//If flag deliverer return false;


public class DeliverItem extends Goal {
    private final GolamEntity deliverer;
    private final double speed;
    private final float searchRadius;
    private BlockPos targetAssignedBlock;
    private boolean awaitingTarget = false;
    private int cooldown = 0;

    public DeliverItem(GolamEntity entity, double speed, float searchRadius) {
        this.deliverer = entity;
        this.speed = speed;
        this.searchRadius = searchRadius;
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (awaitingTarget) return false;

        targetAssignedBlock = deliverer.findAssignedItemGolamInterface(Items.AIR);

        if (targetAssignedBlock != null) {
            deliverer.resetExportFlag();
            return true;
        }
        return deliverer.shouldExport();
    }

    @Override
    public void tick() {
        if (targetAssignedBlock == null) return;
        double distanceSqr = deliverer.distanceToSqr(Vec3.atCenterOf(targetAssignedBlock));
        if (distanceSqr > 4.0) {
            System.out.println("Moving to target: " + targetAssignedBlock);
            deliverer.getNavigation().moveTo(
                    targetAssignedBlock.getX() + 0.5,
                    targetAssignedBlock.getY(),
                    targetAssignedBlock.getZ() + 0.5,
                    speed
            );
            return;
        }
        BlockEntity be = deliverer.level().getBlockEntity(targetAssignedBlock);
        if (!(be instanceof GolamInterfaceBE interfaceBE)) {
            System.out.println("Error: Expected GolamInterfaceBE but found something else at " + targetAssignedBlock);
            return;
        }
        Container chest = interfaceBE.getLinkedChest();
        if (chest == null) {
            System.out.println("No valid chest at interface: " + targetAssignedBlock);
            return;
        }
        if (deliverer.getInventory().getItem(0).isEmpty()) {
            int amountNeeded = 64;
            for (int i = 0; i < chest.getContainerSize(); i++) {
                ItemStack stack = chest.getItem(i);
                int toTake = Math.min(stack.getCount(), amountNeeded);
                if (toTake > 0) {
                    ItemStack pickedItem = stack.copyWithCount(toTake);
                    deliverer.getInventory().setItem(0, pickedItem);
                    stack.shrink(toTake);
                    BlockPos depositTarget = deliverer.findAssignedItemGolamInterface(pickedItem.getItem());
                    if (depositTarget != null) {
                        targetAssignedBlock = depositTarget;
                        return;
                    }
                    awaitingTarget = true;
                    cooldown = 20;
                    return;
                }
            }
        }
        if (!deliverer.getInventory().getItem(0).isEmpty() && distanceSqr <= 4.0) {
            ItemStack heldItem = deliverer.getInventory().getItem(0);
            if (heldItem.isEmpty() || heldItem.getItem() == Items.AIR) return;

            for (int i = 0; i < chest.getContainerSize(); i++) {
                ItemStack slotStack = chest.getItem(i);
                if (slotStack.isEmpty() ||
                        (ItemStack.isSameItem(slotStack, heldItem) && slotStack.getCount() < slotStack.getMaxStackSize())) {
                    int toDeposit = Math.min(heldItem.getCount(), 64 - slotStack.getCount());
                    chest.setItem(i, heldItem.copyWithCount(slotStack.getCount() + toDeposit));
                    heldItem.shrink(toDeposit);
                    if (heldItem.isEmpty()) {
                        deliverer.getInventory().setItem(0, ItemStack.EMPTY);
                        cooldown = 100;
                        targetAssignedBlock = null;
                        awaitingTarget = false;
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void stop() {
        targetAssignedBlock = null;
        awaitingTarget = false;
    }
}