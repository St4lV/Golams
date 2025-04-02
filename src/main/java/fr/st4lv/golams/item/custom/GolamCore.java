package fr.st4lv.golams.item.custom;

import fr.st4lv.golams.block.entity.GolamInterfaceBE;
import fr.st4lv.golams.entity.GolamProfessions;
import fr.st4lv.golams.entity.ModEntities;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GolamCore extends Item {
    private static GolamEntity selectedGolam = null;
    private boolean blockSelected = false;
    private boolean guardGolamSelect = false;
    private GolamEntity guard = null;
    public GolamCore(Properties properties) {
        super(properties);
    }
    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResult.FAIL;
        }

        if (entity instanceof GolamEntity golam) {
            selectedGolam = golam;
            if (player.isShiftKeyDown()){
                switch (golam.getTypeVariant()) {
                    case BLACKSMITH,DELIVERER,HARVESTER:
                        blockSelected = false;
                        selectedGolam.resetAssignedBlock();
                        break;
                    case CARTOGRAPHER:
                        player.getCooldowns().addCooldown(this, 5);
                        return InteractionResult.SUCCESS;
                    case GUARD:
                        guardGolamSelect = false;
                        selectedGolam.resetAssignedGolams();
                        break;
                    default:
                        return InteractionResult.PASS;
                }
                player.displayClientMessage(Component.translatable("interaction.golams.golam_core_assign_clear"), true);
            } else {
                if (guardGolamSelect && selectedGolam.getTypeVariant() != GolamProfessions.GUARD) {
                    guard.addAssignedGolams(selectedGolam.getUUID());
                    guardGolamSelect = false;
                    player.displayClientMessage(Component.translatable("interaction.golams.golam_core_guard_golams_assign_step_2", Component.translatable("entity.golams.golam")), true);
                    selectedGolam.updateGoals();
                    player.getCooldowns().addCooldown(this, 5);
                    return InteractionResult.SUCCESS;
                } else {
                    switch (golam.getTypeVariant()) {
                        case UNASSIGNED:
                            player.displayClientMessage(Component.translatable("interaction.golams.golam_core_unassigned_needs_job"), true);
                            player.getCooldowns().addCooldown(this, 5);
                            return InteractionResult.SUCCESS;
                        case CARTOGRAPHER:
                            if (golam.getItemBySlot(EquipmentSlot.OFFHAND).getItem() == Items.FILLED_MAP) {
                                String poi = golam.getMapPOI();
                                if (poi != null) {
                                    player.displayClientMessage(
                                            Component.translatable("interaction.golams.golam_core_cartographer_valid_poi",
                                            Component.translatable(poi)),
                                            true);
                                } else {
                                    player.displayClientMessage(Component.translatable("interaction.golams.golam_core_cartographer_no_poi"), true);
                                }
                            } else {
                                player.displayClientMessage(Component.translatable("interaction.golams.golam_core_cartographer_no_filled_map", Component.translatable("item.minecraft.filled_map")), true);
                            }
                            break;
                        case GUARD:
                            guard = selectedGolam;
                            guardGolamSelect = true;
                            player.displayClientMessage(Component.translatable("interaction.golams.golam_core_guard_golams_assign_step_1", Component.translatable("entity.golams.golam")), true);
                            break;
                        case BLACKSMITH,DELIVERER,HARVESTER:
                            player.displayClientMessage(Component.translatable("interaction.golams.golam_core_assign_step_1", Component.translatable("block.golams.golam_interface")), true);
                            blockSelected = true;
                            break;
                        default:
                            return InteractionResult.PASS;
                    }
                }
            }
            selectedGolam.updateGoals();
            player.getCooldowns().addCooldown(this, 5);
            return InteractionResult.SUCCESS;
        } else return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (!level.isClientSide) {
            Player player = Objects.requireNonNull(context.getPlayer());
            if (player.getCooldowns().isOnCooldown(this)) {
                return InteractionResult.FAIL;
            }
            ItemStack stack = context.getItemInHand();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);
            BlockEntity be = level.getBlockEntity(pos);
            BlockState block = level.getBlockState(pos);

            if (selectedGolam != null ) {
                switch (selectedGolam.getTypeVariant()) {
                    case UNASSIGNED,CARTOGRAPHER, GUARD:
                        blockSelected=false;
                        return InteractionResult.PASS;
                    case BLACKSMITH,DELIVERER:
                        if (!blockSelected) return InteractionResult.PASS;
                        if (be instanceof GolamInterfaceBE golamInterface) {
                            Item item = golamInterface.inventory.getStackInSlot(0).getItem();
                            selectedGolam.addAssignedBlock(pos, item);
                            golamInterface.addAssignedGolams(selectedGolam.getUUID());
                            selectedGolam.updateGoals();
                            selectedGolam = null;
                            player.displayClientMessage(Component.translatable(
                                    "interaction.golams.golam_core_assign_step_2",
                                    Component.translatable("block.golams.golam_interface"),
                                    String.valueOf(pos.getX()),
                                    String.valueOf(pos.getY()),
                                    String.valueOf(pos.getZ()),
                                    Component.translatable(item.getDescriptionId())
                            ), true);

                            player.getCooldowns().addCooldown(this, 5);
                            return InteractionResult.SUCCESS;
                        }
                        break;
                    case HARVESTER:
                        if (!blockSelected) return InteractionResult.PASS;
                        if (be instanceof GolamInterfaceBE golamInterface) {
                            ItemStack itemStack =golamInterface.inventory.getStackInSlot(0);
                            Item item = itemStack.getItem();
                            if (item == Items.AIR||itemStack.is(ItemTags.HOES)||itemStack.is(ItemTags.AXES)) {
                                selectedGolam.addAssignedBlock(pos, item);
                                golamInterface.addAssignedGolams(selectedGolam.getUUID());
                                selectedGolam.updateGoals();
                                selectedGolam = null;
                                player.getCooldowns().addCooldown(this, 5);
                                return InteractionResult.SUCCESS;
                            } else return InteractionResult.PASS;
                        }
                        if (    //CROPS
                                block.getBlock().defaultBlockState().is(BlockTags.CROPS)
                                ||block.getBlock()==Blocks.SWEET_BERRY_BUSH
                                ||block.getBlock()==Blocks.TORCHFLOWER
                                ||block.getBlock()==Blocks.COCOA
                                ||block.getBlock()==Blocks.NETHER_WART
                                ||block.getBlock()==Blocks.ATTACHED_MELON_STEM
                                ||block.getBlock()== Blocks.ATTACHED_PUMPKIN_STEM
                                ||block.getBlock()== Blocks.COMPOSTER


                                //TREES
                                ||block.getBlock().defaultBlockState().is(BlockTags.SAPLINGS)&&!block.getBlock().defaultBlockState().is(Blocks.MANGROVE_PROPAGULE)
                                ||block.getBlock()==Blocks.BROWN_MUSHROOM
                                ||block.getBlock()==Blocks.RED_MUSHROOM
                                ||block.getBlock()==Blocks.CRIMSON_FUNGUS
                                ||block.getBlock()==Blocks.WARPED_FUNGUS

                                //BEES
                                /*||block.getBlock()==Blocks.BEE_NEST
                                ||block.getBlock()==Blocks.BEEHIVE*/
                        ){
                            Item item;
                            if (block.getBlock()== Blocks.ATTACHED_MELON_STEM){
                                    item = Items.MELON_SEEDS;
                            } else if (block.getBlock()== Blocks.ATTACHED_PUMPKIN_STEM){
                                item = Items.PUMPKIN_SEEDS;
                            } else if (block.getBlock()== Blocks.TORCHFLOWER){
                                item = Items.TORCHFLOWER_SEEDS;
                            } else {
                                item = block.getBlock().asItem();
                            }
                            selectedGolam.addAssignedBlock(pos, item);
                            selectedGolam.updateGoals();
                            selectedGolam = null;

                            player.displayClientMessage(Component.translatable(
                                    "interaction.golams.golam_core_harvester_assign_step_2",
                                    Component.translatable(item.getDescriptionId()),
                                    String.valueOf(pos.getX()),
                                    String.valueOf(pos.getY()),
                                    String.valueOf(pos.getZ())
                            ), true);

                            player.getCooldowns().addCooldown(this, 5);
                            return InteractionResult.SUCCESS;

                        }
                    default:return InteractionResult.PASS;
                    }
                }

            if (state.is(Blocks.BUDDING_AMETHYST)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);

                GolamEntity newGolam = new GolamEntity(ModEntities.GOLAM.get(), level);
                newGolam.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                newGolam.setVariant(GolamProfessions.UNASSIGNED);

                level.addFreshEntity(newGolam);

                /*if (player != null) {
                    //player.displayClientMessage(Component.translatable("!"), true);
                }*/
                Objects.requireNonNull(player).getInventory().removeItem(stack);
                player.getCooldowns().addCooldown(this, 5);
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

}
