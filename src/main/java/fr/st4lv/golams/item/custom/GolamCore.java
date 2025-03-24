package fr.st4lv.golams.item.custom;

import fr.st4lv.golams.block.entity.GolamInterfaceBE;
import fr.st4lv.golams.entity.GolamProfessions;
import fr.st4lv.golams.entity.ModEntities;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
    public GolamCore(Properties properties) {
        super(properties);
    }
    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player, @NotNull LivingEntity entity, @NotNull InteractionHand hand) {
        if (entity instanceof GolamEntity golam) {
            selectedGolam = golam;
            if (player.isShiftKeyDown()){
                blockSelected=false;
                golam.resetAssignedBlock();
                player.displayClientMessage(Component.translatable("interaction.golams.golam_core_assign_clear"), true);
            } else {
                switch (golam.getTypeVariant()) {
                    case CARTOGRAPHER:
                        if (golam.getItemBySlot(EquipmentSlot.OFFHAND).getItem() == Items.FILLED_MAP){
                            String poi = golam.getMapPOI();
                            if (poi!=null) {
                                player.displayClientMessage(Component.translatable("interaction.golams.golam_core_cartographer_valid_poi",poi), true);
                            } else {
                                player.displayClientMessage(Component.translatable("interaction.golams.golam_core_cartographer_no_poi"), true);
                            }
                        } else {
                            player.displayClientMessage(Component.translatable("interaction.golams.golam_core_cartographer_no_filled_map",Component.translatable("item.minecraft.filled_map")),true);
                            break;
                        }
                        break;
                    case GUARD:
                        return InteractionResult.SUCCESS;
                    default:
                        player.displayClientMessage(Component.translatable("interaction.golams.golam_core_assign_step_1",Component.translatable("block.golams.golam_interface")), true);
                        blockSelected=true;
                        break;
                }
            }
            selectedGolam.updateGoals();
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(stack, player, entity, hand);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        if (!level.isClientSide) {
            Player player = context.getPlayer();
            ItemStack stack = context.getItemInHand();
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);
            BlockEntity be = level.getBlockEntity(pos);

            if (selectedGolam != null && player!=null ) {
                if (!blockSelected) return InteractionResult.PASS;
                if (be instanceof GolamInterfaceBE golamInterface) {
                    Item item = golamInterface.inventory.getStackInSlot(0).getItem();
                    selectedGolam.addAssignedBlock(pos, item);
                    selectedGolam.updateGoals();
                    selectedGolam = null;

                    player.displayClientMessage(Component.translatable(
                            "interaction.golams.golam_core_assign_step_2",
                            Component.translatable("block.golams.golam_interface"),
                            String.valueOf(pos.getX()),
                            String.valueOf(pos.getY()),
                            String.valueOf(pos.getZ()),
                            ((GolamInterfaceBE) be).inventory.getStackInSlot(0).getItem().getDescription()
                    ), true);


                    return InteractionResult.SUCCESS;
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
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

}
