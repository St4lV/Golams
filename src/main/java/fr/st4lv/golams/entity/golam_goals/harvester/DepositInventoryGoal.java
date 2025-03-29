package fr.st4lv.golams.entity.golam_goals.harvester;

import fr.st4lv.golams.block.entity.GolamInterfaceBE;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Objects;

public class DepositInventoryGoal extends Goal {

    private GolamEntity entity;
    private final double speed;

    private BlockPos targetBlock;
    private int cooldown;

    public DepositInventoryGoal(GolamEntity entity, double speed) {
        this.entity = entity;
        this.speed = speed;
        this.cooldown = 0;
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }
        if (entity.getInventory().isEmpty()) return false;

        targetBlock = selectGolamInterfaceWithAir();
        cooldown = 80;
        return targetBlock != null;
    }

    private BlockPos selectGolamInterfaceWithAir(){
        List<GolamEntity.AssignedBlock> targetEntityBlocksList = entity.getAssignedBlocks();
        if (targetEntityBlocksList.isEmpty()) return null;
        Level level = entity.level();
        for (GolamEntity.AssignedBlock ab : targetEntityBlocksList) {
            BlockPos pos = ab.getBlockPos();
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GolamInterfaceBE golamInterface) {
                Item item = golamInterface.inventory.getStackInSlot(0).getItem();
                if (item == Items.AIR) {
                    return pos;
                }
            }
        }
        return null;
    }

    @Override
    public void tick() {
        if (targetBlock == null) return;
        double distanceSqr = entity.distanceToSqr(Vec3.atCenterOf(targetBlock));
        entity.getNavigation().moveTo(
                targetBlock.getX() + 0.5,
                targetBlock.getY(),
                targetBlock.getZ() + 0.5,
                speed
        );
        if (distanceSqr < 3.0) {
            BlockEntity be = entity.level().getBlockEntity(targetBlock);
            if (!(be instanceof GolamInterfaceBE interfaceBE)) return;

            Container chest = interfaceBE.getLinkedChest();
            if (chest == null) return;

            if (chest instanceof ChestBlockEntity chestEntity)
                chest = getChestContainer(chestEntity);

            for (int invSlot = 0; invSlot < entity.getInventory().getContainerSize(); invSlot++) {
                ItemStack held = entity.getInventory().getItem(invSlot);
                if (held.isEmpty() || held.getItem() == Items.AIR)
                    continue;

                for (int chestSlot = 0; chestSlot < chest.getContainerSize(); chestSlot++) {
                    ItemStack slotStack = chest.getItem(chestSlot);

                    if (slotStack.isEmpty() ||
                            (ItemStack.isSameItem(slotStack, held) && slotStack.getCount() < slotStack.getMaxStackSize())) {

                        int maxStack = slotStack.isEmpty() ? held.getMaxStackSize() : slotStack.getMaxStackSize();
                        int toDeposit = Math.min(held.getCount(), maxStack - slotStack.getCount());

                        chest.setItem(chestSlot, held.copyWithCount(slotStack.getCount() + toDeposit));
                        held.shrink(toDeposit);

                        if (held.isEmpty()) {
                            entity.getInventory().setItem(invSlot, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static Container getChestContainer(ChestBlockEntity chestEntity) {
        Level level = chestEntity.getLevel();
        BlockState state = chestEntity.getBlockState();

        if (state.getBlock() instanceof ChestBlock chestBlock) {
            return ChestBlock.getContainer(chestBlock, state, Objects.requireNonNull(level), chestEntity.getBlockPos(), true);
        }
        return chestEntity;
    }
}
