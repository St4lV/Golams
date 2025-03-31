package fr.st4lv.golams.entity.golam_goals.harvester;

import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;

import java.util.*;

import static net.minecraft.world.level.block.SweetBerryBushBlock.AGE;

public class HarvestAssignedRessourcesGoal extends Goal {

    private GolamEntity entity;
    private final double speed;

    private BlockPos targetBlock;
    private int cooldown;
    private int action_cooldown;
    private boolean harvesting = false;
    private final List<BlockPos> blocksToBreak = new ArrayList<>();
    private final List<BlockPos> treeBaseBlocks = new ArrayList<>();


    public HarvestAssignedRessourcesGoal(GolamEntity entity, double speed) {
        this.entity = entity;
        this.speed = speed;
        this.cooldown = 0;
    }

    @Override
    public boolean canUse() {
        if (!entity.getInventory().isEmpty()) return false;
        if (cooldown > 0) {
            cooldown--;
            return false;
        } else {harvesting=false;}
        if (entity.getAssignedBlocks().isEmpty()) return false;


        cooldown = 500;
        if (harvesting)return false;
        targetBlock=assignBlockFromList();
        harvesting=true;
        return targetBlock != null;
    }

    private BlockPos assignBlockFromList(){
        List<GolamEntity.AssignedBlock> targetEntityBlocksList = entity.getAssignedBlocks();
        if (targetEntityBlocksList.isEmpty()) return null;

        for (GolamEntity.AssignedBlock ab : targetEntityBlocksList) {
            BlockPos pos = ab.getBlockPos();

            BlockState blockState = entity.level().getBlockState(pos);
            ItemStack itemstack = entity.getItemBySlot(EquipmentSlot.MAINHAND);
            //CROPS
            if (itemstack.is(ItemTags.HOES)){
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
                    if (blockState.getBlock() instanceof CropBlock cropBlock) {
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
            else if (itemstack.is(ItemTags.AXES)) {
                if (blockState.is(BlockTags.LOGS)) {
                    return pos;
                }
            }
        }
        return null;
    }

    @Override
    public void tick() {
        action_cooldown++;
        if (targetBlock == null) return;
        entity.getNavigation().moveTo(
                targetBlock.getX() + 0.5,
                targetBlock.getY(),
                targetBlock.getZ() + 0.5,
                speed
        );
        double distanceSqr = entity.distanceToSqr(Vec3.atCenterOf(targetBlock));
        if (distanceSqr < 2.6) {
            Level level = entity.level();
            if (!(level instanceof ServerLevel serverLevel)) return;

            BlockState blockState = level.getBlockState(targetBlock);
            ItemStack itemstack = entity.getItemBySlot(EquipmentSlot.MAINHAND);
            List<ItemStack> drops = Block.getDrops(blockState, serverLevel, targetBlock, level.getBlockEntity(targetBlock), entity, ItemStack.EMPTY);
            if (itemstack.is(ItemTags.HOES)){
                if (    blockState.is(BlockTags.CROPS)
                        ||blockState.getBlock()==Blocks.SWEET_BERRY_BUSH
                        ||blockState.getBlock()==Blocks.TORCHFLOWER
                        ||blockState.getBlock()==Blocks.COCOA
                        ||blockState.getBlock()==Blocks.NETHER_WART
                        ||blockState.getBlock() == Blocks.ATTACHED_MELON_STEM
                        ||blockState.getBlock() == Blocks.ATTACHED_PUMPKIN_STEM
                ) {
                    // CROPS

                    if (blockState.getBlock() == Blocks.TORCHFLOWER) {
                        level.setBlock(targetBlock, Blocks.TORCHFLOWER_CROP.defaultBlockState(), 3);
                    } else if (blockState.getBlock() == Blocks.ATTACHED_MELON_STEM || blockState.getBlock() == Blocks.ATTACHED_PUMPKIN_STEM) {
                        Direction facing = blockState.getValue(HorizontalDirectionalBlock.FACING);
                        BlockPos facingBlock = targetBlock.relative(facing);
                        BlockState facingBlockState = level.getBlockState(facingBlock);
                        if (facingBlockState.getBlock() == Blocks.MELON || facingBlockState.getBlock() == Blocks.PUMPKIN) {
                            drops = Block.getDrops(facingBlockState, serverLevel, facingBlock, level.getBlockEntity(facingBlock), entity, ItemStack.EMPTY);
                            level.removeBlock(facingBlock, false);
                        }
                    } else if (blockState.getBlock().getClass() == SweetBerryBushBlock.class) {
                        level.setBlock(targetBlock, blockState.setValue(AGE, 1), 3);
                    } else if (blockState.getBlock() instanceof PitcherCropBlock) {
                        BlockPos underpart;
                        BlockPos upperpart;
                        if (blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER) {
                            underpart = targetBlock.below();
                            upperpart = targetBlock;
                        } else {
                            upperpart = targetBlock.above();
                            underpart = targetBlock;
                        }
                        drops = Block.getDrops(blockState, serverLevel, underpart, level.getBlockEntity(underpart), entity, ItemStack.EMPTY);
                        level.setBlock(underpart, blockState.setValue(PitcherCropBlock.AGE, 0), 3);
                        level.removeBlock(upperpart, false);
                    } else if (blockState.getBlock() instanceof CocoaBlock) {
                        level.setBlock(targetBlock, blockState.setValue(CocoaBlock.AGE, 0), 3);
                    } else {
                        level.setBlock(targetBlock, blockState.getBlock().defaultBlockState(), 3);
                    }
                    harvesting=false;

                    if (assignBlockFromList() != null) {
                        targetBlock = assignBlockFromList();
                    } else {
                        targetBlock = null;
                    }
                }
                 }
            if (itemstack.is(ItemTags.AXES)){
                if (blockState.is(BlockTags.LOGS)) {

                    //LOGS / STEMS

                    drops.clear();
                    Block replantBlock=null;

                    if (blockState.getBlock() == Blocks.OAK_LOG) replantBlock = Blocks.OAK_SAPLING;
                    if (blockState.getBlock() == Blocks.SPRUCE_LOG) replantBlock = Blocks.SPRUCE_SAPLING;
                    if (blockState.getBlock() == Blocks.BIRCH_LOG) replantBlock = Blocks.BIRCH_SAPLING;
                    if (blockState.getBlock() == Blocks.JUNGLE_LOG) replantBlock = Blocks.JUNGLE_SAPLING;
                    if (blockState.getBlock() == Blocks.ACACIA_LOG) replantBlock = Blocks.ACACIA_SAPLING;
                    if (blockState.getBlock() == Blocks.DARK_OAK_LOG) replantBlock = Blocks.DARK_OAK_SAPLING;
                    if (blockState.getBlock() == Blocks.CRIMSON_STEM) replantBlock = Blocks.CRIMSON_FUNGUS;
                    if (blockState.getBlock() == Blocks.WARPED_STEM) replantBlock = Blocks.WARPED_FUNGUS;
                    if (blockState.getBlock() == Blocks.MANGROVE_LOG) replantBlock = Blocks.MANGROVE_PROPAGULE;
                    if (blockState.getBlock() == Blocks.CHERRY_LOG) replantBlock = Blocks.CHERRY_SAPLING;

                    if (!blocksToBreak.contains(targetBlock)) {
                        blocksToBreak.add(targetBlock);
                    }
                    boolean isBigTree = false;

                    for (int i = 0; i >= -1; --i) {
                        for (int j = 0; j >= -1; --j) {
                            if (isTwoByTwoTree(blockState, level, targetBlock, i, j)) {
                                isBigTree = true;
                                treeBaseBlocks.add(targetBlock.offset(i, 0, j));
                                treeBaseBlocks.add(targetBlock.offset(i + 1, 0, j));
                                treeBaseBlocks.add(targetBlock.offset(i, 0, j + 1));
                                treeBaseBlocks.add(targetBlock.offset(i + 1, 0, j + 1));
                            }
                        }
                    }

                    if (!isBigTree && !treeBaseBlocks.contains(targetBlock)) {
                        treeBaseBlocks.add(targetBlock);
                    }

                    Queue<BlockPos> queue = new LinkedList<>();
                    Set<BlockPos> visited = new HashSet<>();
                    queue.add(targetBlock.above());


                    while (!queue.isEmpty()) {
                        BlockPos currentPos = queue.poll();
                        BlockState currentState = level.getBlockState(currentPos);

                        if (currentState.is(BlockTags.LOGS) && !blocksToBreak.contains(currentPos)) {
                            blocksToBreak.add(currentPos);
                            visited.add(currentPos);

                            for (int x = -2; x <= 2; x++) {
                                for (int y = -1; y <= 1; y++) {
                                    for (int z = -2; z <= 2; z++) {
                                        if (x == 0 && y == 0 && z == 0) continue;
                                        BlockPos newPos = currentPos.offset(x, y, z);
                                        if (!visited.contains(newPos) && level.getBlockState(newPos).is(BlockTags.LOGS)) {
                                            queue.add(newPos);
                                            visited.add(newPos);
                                        }
                                    }
                                }
                            }

                            BlockPos abovePos = currentPos.above();
                            if (!visited.contains(abovePos) && level.getBlockState(abovePos).is(BlockTags.LOGS)) {
                                queue.add(abovePos);
                                visited.add(abovePos);
                            }
                        }
                    }
                    for (BlockPos base : treeBaseBlocks) {
                        BlockPos currentPos = base;
                        while (level.getBlockState(currentPos).is(BlockTags.LOGS)
                                || level.getBlockState(currentPos).is(BlockTags.LEAVES)
                                || level.getBlockState(currentPos).is(BlockTags.WART_BLOCKS)) {
                            if (!blocksToBreak.contains(currentPos)) {
                                blocksToBreak.add(currentPos);
                            }
                            currentPos = currentPos.above();
                        }
                    }

                    List<BlockPos> leavesToBreak = new ArrayList<>();

                    for (BlockPos logPos : blocksToBreak) {
                        for (int x = -4; x <= 4; x++) {
                            for (int y = 0; y <= 4; y++) {
                                for (int z = -4; z <= 4; z++) {
                                    BlockPos pos = logPos.offset(x, y, z);
                                    BlockState state = level.getBlockState(pos);
                                    if ((state.is(BlockTags.LEAVES) || state.is(BlockTags.WART_BLOCKS)
                                            || state.getBlock() == Blocks.SHROOMLIGHT
                                            || state.getBlock() == Blocks.WEEPING_VINES)
                                            && !leavesToBreak.contains(pos)) {
                                        leavesToBreak.add(pos);
                                    }

                                }
                            }
                        }
                    }
                    for (BlockPos pos : leavesToBreak) {
                        BlockState state = level.getBlockState(pos);
                        List<ItemStack> blockDrops = Block.getDrops(state, serverLevel, pos, level.getBlockEntity(pos), entity, ItemStack.EMPTY);
                        level.destroyBlock(pos, false);
                        drops.addAll(blockDrops);
                    }
                    leavesToBreak.clear();
                    for (BlockPos pos : blocksToBreak) {
                        BlockState state = level.getBlockState(pos);
                        List<ItemStack> blockDrops = Block.getDrops(state, serverLevel, pos, level.getBlockEntity(pos), entity, ItemStack.EMPTY);
                        level.destroyBlock(pos, false);
                        drops.addAll(blockDrops);
                    }
                    blocksToBreak.clear();

                    if (replantBlock!=null){
                        for (BlockPos basePos : treeBaseBlocks) {
                            if (level.getBlockState(basePos).isAir()) {
                                level.setBlock(basePos, replantBlock.defaultBlockState(), 3);
                            }
                        }
                    }
                    treeBaseBlocks.clear();
                    if (assignBlockFromList() != null) {
                        targetBlock = assignBlockFromList();
                    } else {
                        targetBlock = null;
                    }

                }
            }
            action_cooldown = 0;
            harvesting=false;
            for (ItemStack item : drops) {
                entity.getInventory().addItem(item);
            }

        }
    }

    @Override
    public boolean canContinueToUse() {
        if (action_cooldown>=500){
            action_cooldown=0;
            return false;
        }
        return targetBlock != null;
    }
    //Mojang code for detecting 2x2 saplings (-> TreeGrower.isTwoByTwoSapling )
    private static boolean isTwoByTwoTree(BlockState state, BlockGetter level, BlockPos pos, int xOffset, int yOffset) {
        Block block = state.getBlock();
        return level.getBlockState(pos.offset(xOffset, 0, yOffset)).is(block) && level.getBlockState(pos.offset(xOffset + 1, 0, yOffset)).is(block) && level.getBlockState(pos.offset(xOffset, 0, yOffset + 1)).is(block) && level.getBlockState(pos.offset(xOffset + 1, 0, yOffset + 1)).is(block);
    }
}
