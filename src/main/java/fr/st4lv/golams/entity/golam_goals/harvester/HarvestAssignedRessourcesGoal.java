package fr.st4lv.golams.entity.golam_goals.harvester;

import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.world.level.block.SweetBerryBushBlock.AGE;

public class HarvestAssignedRessourcesGoal extends Goal {

    private GolamEntity entity;
    private final double speed;

    private BlockPos targetBlock;
    private int cooldown;

    public HarvestAssignedRessourcesGoal(GolamEntity entity, double speed) {
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
        if (entity.getAssignedBlocks().isEmpty()) return false;


        cooldown = 200;
        targetBlock=assignBlockFromList();
        System.out.println(targetBlock);
        return targetBlock != null;
    }

    private BlockPos assignBlockFromList(){
        List<GolamEntity.AssignedBlock> targetEntityBlocksList = entity.getAssignedBlocks();
        if (targetEntityBlocksList.isEmpty()) return null;

        for (GolamEntity.AssignedBlock ab : targetEntityBlocksList) {
            BlockPos pos = ab.getBlockPos();

            BlockState blockState = entity.level().getBlockState(pos);
            System.out.println(blockState.getBlock()+" | "+pos);
            //CROPS
            if (    blockState.is(BlockTags.CROPS)
                    ||blockState.getBlock()==Blocks.SWEET_BERRY_BUSH
                    ||blockState.getBlock()==Blocks.TORCHFLOWER
                    ||blockState.getBlock()==Blocks.COCOA
                    ||blockState.getBlock()==Blocks.NETHER_WART
                    ||blockState.getBlock() == Blocks.ATTACHED_MELON_STEM
                    ||blockState.getBlock() == Blocks.ATTACHED_PUMPKIN_STEM
            ) {
                if (blockState.getBlock() == Blocks.ATTACHED_MELON_STEM || blockState.getBlock() == Blocks.ATTACHED_PUMPKIN_STEM || blockState.getBlock() == Blocks.TORCHFLOWER) return pos;
                if (blockState.getBlock() == Blocks.PITCHER_CROP ) {
                    PitcherCropBlock pitcherCropBlock = (PitcherCropBlock) blockState.getBlock();
                    if (!pitcherCropBlock.isValidBonemealTarget(entity.level(),pos,blockState)) {
                        return pos;
                    }
                }
                if (blockState.getBlock() instanceof CropBlock) {
                    CropBlock cropBlock = (CropBlock) blockState.getBlock();
                    if (cropBlock.getAge(blockState) == cropBlock.getMaxAge()) {
                        if (blockState.getBlock() == Blocks.MELON_STEM || blockState.getBlock() == Blocks.PUMPKIN_STEM) {
                            return null;
                        }
                        return pos;
                    }
                } else
                if (blockState.getBlock().getClass()== SweetBerryBushBlock.class) {
                    if (blockState.getValue(AGE) == SweetBerryBushBlock.MAX_AGE) {
                        return pos;
                    }
                } else
                if (blockState.getBlock().getClass()== NetherWartBlock.class) {
                    if (blockState.getValue(AGE) == NetherWartBlock.MAX_AGE) {
                        return pos;
                    }
                }
                if (blockState.getBlock().getClass()== CocoaBlock.class){
                    CocoaBlock cocoaBlock = (CocoaBlock) blockState.getBlock();
                    if (!cocoaBlock.isValidBonemealTarget(entity.level(),pos,blockState)) {
                        return pos;
                    }
                }

            }
        }
        return null;
    }

    @Override
    public void tick() {
        if (targetBlock == null) return;
        double distanceSqr = entity.distanceToSqr(Vec3.atCenterOf(targetBlock));
        if (distanceSqr > 2.0) {
            entity.getNavigation().moveTo(
                    targetBlock.getX() + 0.5,
                    targetBlock.getY(),
                    targetBlock.getZ() + 0.5,
                    speed
            ); return;
        }
    }
}
