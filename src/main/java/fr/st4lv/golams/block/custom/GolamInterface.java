package fr.st4lv.golams.block.custom;

import com.mojang.serialization.MapCodec;
import fr.st4lv.golams.block.entity.GolamInterfaceBE;
import fr.st4lv.golams.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GolamInterface extends BaseEntityBlock implements EntityBlock{
    private static final VoxelShape SHAPE_DOWN = Block.box(3, 0, 3, 13, 2, 13);
    private static final VoxelShape SHAPE_UP = Block.box(3, 14, 3, 13, 16, 13);
    private static final VoxelShape SHAPE_NORTH = Block.box(3, 3, 0, 13, 13, 2);
    private static final VoxelShape SHAPE_SOUTH = Block.box(3, 3, 14, 13, 13, 16);
    private static final VoxelShape SHAPE_WEST = Block.box(0, 3, 3, 2, 13, 13);
    private static final VoxelShape SHAPE_EAST = Block.box(14, 3, 3, 16, 13, 13);

    public static final MapCodec<GolamInterface> CODEC = simpleCodec(GolamInterface::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty POWERED;

    protected GolamInterface(BlockBehaviour.Properties properties) {

        super(properties);
        this.registerDefaultState((BlockState) this.defaultBlockState().setValue(POWERED, false));
    }

    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            boolean flag = (Boolean) state.getValue(POWERED);
            if (flag != level.hasNeighborSignal(pos)) {
                if (flag) {
                    level.scheduleTick(pos, this, 4);
                } else {
                    level.setBlock(pos, (BlockState) state.cycle(POWERED), 2);
                }
            }
        }

    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if ((Boolean) state.getValue(POWERED) && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, (BlockState) state.cycle(POWERED), 2);
        }
    }


    @Override
    public @NotNull VoxelShape getShape(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> SHAPE_DOWN;
            case UP -> SHAPE_UP;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return new GolamInterfaceBE(blockPos, blockState);
    }

    @Override
    protected void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof GolamInterfaceBE golamBE) {
                golamBE.inventory.setStackInSlot(0, ItemStack.EMPTY);
                golamBE.drops();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }


    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.getBlockEntity(pos) instanceof GolamInterfaceBE golamBE) {

            if (player.isShiftKeyDown()) {
                /*if (itemstack.is(Items.AMETHYST_SHARD)){
                    player.openMenu(new SimpleMenuProvider(golamBE, Component.literal("Golam Interface")), pos);

                } else {
                }*/
                return ItemInteractionResult.SUCCESS;
            } else {
                if (!stack.isEmpty()) {
                    golamBE.inventory.insertItem(0, stack.copy(), false);
                    level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
                } else {
                    golamBE.clearContents();
                    level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
                }
            }
        }

        return ItemInteractionResult.SUCCESS;
    }

    static {
        POWERED = BlockStateProperties.POWERED;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == ModBlockEntities.GOLAM_INTERFACE_BE.get() ? (BlockEntityTicker<T>) GolamInterfaceBE::tick : null;

    }
}
