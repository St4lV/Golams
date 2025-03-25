package fr.st4lv.golams.block.entity;

import fr.st4lv.golams.block.custom.GolamInterface;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GolamInterfaceBE extends BlockEntity implements MenuProvider {

    private final List<AssignedGolams> assignedGolams = new ArrayList<>();

    public List<AssignedGolams> getAssignedGolams() {
        return this.assignedGolams;
    }
    public void addAssignedGolams(UUID uuid) {
        for (int i = 0; i < assignedGolams.size(); i++) {
            AssignedGolams ag = assignedGolams.get(i);
            if (ag.getGolamUuid().equals(uuid)) {
                assignedGolams.set(i, new AssignedGolams(uuid));
                return;
            }
        }
        assignedGolams.add(new AssignedGolams(uuid));
    }

    public static class AssignedGolams {
        private final UUID uuid;

        public AssignedGolams(UUID uuid) {
            this.uuid = uuid;
        }

        public UUID getGolamUuid() {
            return uuid;
        }
    }

    public void removeAssignedGolam(UUID uuid) {
        assignedGolams.removeIf(ag -> ag.getGolamUuid().equals(uuid));
    }

    public void resetAssignedGolams(){
        assignedGolams.clear();
    }
    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected int getStackLimit(int slot, @NotNull ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            assert level != null;
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public GolamInterfaceBE(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.GOLAM_INTERFACE_BE.get(), pos, blockState);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("block.golams.golam_interface");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        return null;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag assignedGolamsTag = new ListTag();
        for (AssignedGolams ag : assignedGolams) {
            CompoundTag blockTag = new CompoundTag();

            UUID uuid = ag.getGolamUuid();
            blockTag.putUUID("uuid", uuid);
            assignedGolamsTag.add(blockTag);
        }
        tag.put("assigned_golams", assignedGolamsTag);
        tag.put("Inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        assignedGolams.clear();
        ListTag assignedBlocksTag = tag.getList("assigned_golams", Tag.TAG_COMPOUND);
        for (Tag tag_ : assignedBlocksTag) {
            if (tag_ instanceof CompoundTag blockTag) {
                UUID uuid = blockTag.getUUID("uuid");
                assignedGolams.add(new AssignedGolams(uuid));
            }
        }
    }
    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        this.loadAdditional(tag, lookupProvider);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    public void clearContents() {
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for(int i = 0; i < inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }

        assert this.level != null;
        Containers.dropContents(this.level, this.worldPosition, inv);
    }
    
    @Nullable
    public Container getLinkedChest() {
        Direction facing = this.getBlockState().getValue(GolamInterface.FACING);
        BlockPos chestPos = this.worldPosition.relative(facing);
        BlockEntity be = Objects.requireNonNull(this.level).getBlockEntity(chestPos);

        if (be instanceof Container) {
            return (Container) be;
        }

        return null;
    }

    private static Container getChestContainer(ChestBlockEntity chestEntity) {
        Level level = chestEntity.getLevel();
        BlockState state = chestEntity.getBlockState();

        if (state.getBlock() instanceof ChestBlock chestBlock) {
            assert level != null;
            return ChestBlock.getContainer(chestBlock, state, level, chestEntity.getBlockPos(), true);
        }
        return chestEntity;
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState state, T t) {
        if (!(t instanceof GolamInterfaceBE blockEntity)) return;
        if (!((GolamInterfaceBE) t).interfaceEmtpy()) return;
        if (!state.getValue(GolamInterface.POWERED)) {
            Container chest = blockEntity.getLinkedChest();
            if (chest == null) return;
            if (chest instanceof ChestBlockEntity chestEntity) chest = getChestContainer(chestEntity);
            if (chest.isEmpty()) return;
            for (int i = 0; i < blockEntity.getAssignedGolams().size(); i++) {
                AssignedGolams ag = blockEntity.getAssignedGolams().get(i);
                UUID golamUUID = ag.getGolamUuid();
                for (Entity entity : Objects.requireNonNull(blockEntity.getLevel()).getEntities(null, new AABB(blockEntity.getBlockPos()).inflate(100))) {
                    if (entity instanceof GolamEntity golam && entity.getUUID().equals(golamUUID)) {
                        golam.requestExport();
                    }
                }
            }
        }
    }
public boolean interfaceEmtpy() {
    return this.inventory.getStackInSlot(0).isEmpty();
}
}
