package fr.st4lv.golams.entity.custom;

import fr.st4lv.golams.block.entity.GolamInterfaceBE;
import fr.st4lv.golams.data_component.ModDataComponents;
import fr.st4lv.golams.entity.GolamProfessions;
import fr.st4lv.golams.entity.golam_goals.FollowOtherGolamsGoal;
import fr.st4lv.golams.entity.golam_goals.blacksmith.HealOtherGolamsGoal;
import fr.st4lv.golams.entity.golam_goals.blacksmith.RestockSmoothBasaltGoal;
import fr.st4lv.golams.entity.golam_goals.cartographer.ReachPoiGoal;
import fr.st4lv.golams.entity.golam_goals.deliverer.ExportItemGoal;
import fr.st4lv.golams.entity.golam_goals.deliverer.InsertItemGoal;
import fr.st4lv.golams.entity.golam_goals.guard.FollowAssignedGolamGoal;
import fr.st4lv.golams.entity.golam_goals.harvester.HarvestAssignedRessourcesGoal;
import fr.st4lv.golams.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GolamEntity extends AbstractGolem implements InventoryCarrier, NeutralMob {

    public static final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 2;
    private static final int ALERT_RANGE_Y = 10;
    public static float repair_value_by_smooth_basalt = 2;
    private final SimpleContainer inventory = new SimpleContainer(9);
    private boolean persistenceRequired;
    private final List<AssignedBlock> assignedBlocks = new ArrayList<>();

    private static final EntityDataAccessor<String> GOLAM_PROFESSION =
            SynchedEntityData.defineId(GolamEntity.class, EntityDataSerializers.STRING);

    private int remainingPersistentAngerTime;

    public GolamEntity(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected int decreaseAirSupply(int currentAir) {
        return super.decreaseAirSupply(0);
    }

    @Override
    protected void registerGoals() {
        // ALL
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());

        this.goalSelector.addGoal(5, new FollowOtherGolamsGoal(this, 0.8, 32.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(20, new WaterAvoidingRandomStrollGoal(this, 0.5));
        this.goalSelector.addGoal(10, new MoveTowardsTargetGoal(this, 0.9, 32.0F));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));

        //BY PROFESSION
        switch (getTypeVariant()) {
            case BLACKSMITH :
                this.goalSelector.addGoal(2, new HealOtherGolamsGoal(this, 1.05, 32.0F));
                this.goalSelector.addGoal(1, new RestockSmoothBasaltGoal(this, 1.0));
                break;
            case CARTOGRAPHER:
                this.goalSelector.addGoal(1, new ReachPoiGoal(this,1.0));
                break;
            case DELIVERER:
                this.goalSelector.addGoal(2, new ExportItemGoal(this, 1.0));
                this.goalSelector.addGoal(1, new InsertItemGoal(this, 1.0));
                break;
            case GUARD:
                this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.9, true));
                this.goalSelector.addGoal(2, new FollowAssignedGolamGoal(this,0.9));
                this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
                this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mob.class, 5, false, false, (p_28879_) -> p_28879_ instanceof Enemy && !(p_28879_ instanceof Creeper)));
                break;
            case HARVESTER:
                this.goalSelector.addGoal(1, new HarvestAssignedRessourcesGoal(this,1.0));
            default:
                break;
        }

    }

    public void updateGoals() {
        this.goalSelector.getAvailableGoals().clear();
        if (getTypeVariant() == GolamProfessions.GUARD) {
            applyDynamicHealthModifier(this);
        }
        registerGoals();
    }
    private boolean needsRestocking = false;

    public void requestRestock() {
        this.needsRestocking = true;
    }

    public boolean needsRestocking() {
        return needsRestocking;
    }

    public void resetRestockFlag() {
        this.needsRestocking = false;
    }

    private boolean shouldExport = false;

    public void requestExport() {
        this.shouldExport = true;
    }

    public boolean shouldExport() {
        return shouldExport;
    }

    public void resetExportFlag() {
        this.shouldExport = false;
    }

    private boolean shouldReachPoi = false;

    public void ReachPoi() {
        this.shouldReachPoi = true;
    }

    public boolean shouldReachPoi() {
        return shouldReachPoi;
    }

    public void resetReachPoi() {
        this.shouldReachPoi = false;
    }

    private final List<AssignedGolams> assignedGolams = new ArrayList<>();

    public List<AssignedGolams> getAssignedGolams() {
        return this.assignedGolams;
    }
    public void addAssignedGolams(UUID uuid) {
        resetAssignedGolams();
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

    public BlockPos findAssignedItemGolamInterface(Item item) {
        for (AssignedBlock ab : assignedBlocks) {
            if (ab.getItem() == item) {
                BlockPos pos = ab.getBlockPos();
                BlockEntity be = this.level().getBlockEntity(pos);
                if (be instanceof GolamInterfaceBE interfaceBE) {
                    ItemStack slot0 = interfaceBE.inventory.getStackInSlot(0);
                    if (slot0.getItem() == item ) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    public List<AssignedBlock> getAssignedBlocks() {
        return this.assignedBlocks;
    }


    public void addAssignedBlock(BlockPos blockPos, Item item) {
        for (int i = 0; i < assignedBlocks.size(); i++) {
            AssignedBlock ab = assignedBlocks.get(i);
            if (ab.getBlockPos().equals(blockPos)) {
                assignedBlocks.set(i, new AssignedBlock(blockPos, item));
                return;
            }
        }
        assignedBlocks.add(new AssignedBlock(blockPos, item));
    }

    public void resetAssignedBlock(){
        assignedBlocks.clear();
    }


    public String getMapPOI() {
        if (getTypeVariant() != GolamProfessions.CARTOGRAPHER) {
            return null;
        }

        ItemStack mapStack = getItemBySlot(EquipmentSlot.OFFHAND);
        if (mapStack.getItem() != Items.FILLED_MAP || !mapStack.has(DataComponents.MAP_DECORATIONS)) {
            return null;
        }

        MapDecorations decorations = mapStack.get(DataComponents.MAP_DECORATIONS);
        if (decorations == null || decorations.decorations().isEmpty()) {
            return null;
        }
        MapDecorations.Entry entry = decorations.decorations().values().iterator().next();

        int x = (int) entry.x();
        int z = (int) entry.z();
        String poiType = entry.type().getRegisteredName();

        BlockPos poiPos = new BlockPos(x, 64, z);
        addAssignedBlock(poiPos, mapStack.getItem());
        ReachPoi();
        return poiType;
    }

    public void applyDynamicHealthModifier(Mob entity) {
        if (entity != null && entity.isAlive()) {
            AttributeInstance healthAttribute = Objects.requireNonNull(entity.getAttribute(Attributes.MAX_HEALTH));
            ResourceLocation modifierId = ResourceLocation.withDefaultNamespace("health");
            boolean hasExistingModifier = false;
            for (AttributeModifier modifier : healthAttribute.getModifiers()) {
                if (modifier.id().equals(modifierId)) {
                    hasExistingModifier = true;
                    break;}}
            if (!hasExistingModifier) {
                AttributeModifier healthModifier = new AttributeModifier(modifierId, 10.0, AttributeModifier.Operation.ADD_VALUE);
                healthAttribute.addPermanentModifier(healthModifier);
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10d)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.ATTACK_DAMAGE,5.0f)
                .add(Attributes.STEP_HEIGHT,1.0);

    }


    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GOLAM_PROFESSION, GolamProfessions.UNASSIGNED.name());
    }

    public GolamProfessions getTypeVariant() {
        return GolamProfessions.valueOf(this.entityData.get(GOLAM_PROFESSION));
    }

    public void setVariant(GolamProfessions variant) {
        this.entityData.set(GOLAM_PROFESSION, variant.name());
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString("golam_profession", this.getTypeVariant().getProfessionName());
        compound.putBoolean("PersistenceRequired", true);

        ListTag assignedBlocksTag = new ListTag();
        for (AssignedBlock assignedBlock : assignedBlocks) {
            CompoundTag blockTag = new CompoundTag();

            BlockPos pos = assignedBlock.getBlockPos();
            blockTag.putInt("x", pos.getX());
            blockTag.putInt("y", pos.getY());
            blockTag.putInt("z", pos.getZ());
            blockTag.putString("item", BuiltInRegistries.ITEM.getKey(assignedBlock.getItem()).toString());
            assignedBlocksTag.add(blockTag);
        }
        compound.put("assigned_blocks", assignedBlocksTag);
        ListTag assignedGolamsTag = new ListTag();
        for (AssignedGolams ag : assignedGolams) {
            CompoundTag blockTag = new CompoundTag();

            UUID uuid = ag.getGolamUuid();
            blockTag.putUUID("uuid", uuid);
            assignedGolamsTag.add(blockTag);
        }
        compound.put("assigned_golams", assignedGolamsTag);
        this.writeInventoryToTag(compound, this.registryAccess());
    }



    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setVariant(GolamProfessions.byName(compound.getString("golam_profession")));
        this.persistenceRequired = compound.getBoolean("PersistenceRequired");

        assignedBlocks.clear();
        ListTag assignedBlocksTag = compound.getList("assigned_blocks", Tag.TAG_COMPOUND);
        for (Tag tag : assignedBlocksTag) {
            if (tag instanceof CompoundTag blockTag) {
                int x = blockTag.getInt("x");
                int y = blockTag.getInt("y");
                int z = blockTag.getInt("z");
                BlockPos pos = new BlockPos(x, y, z);
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(blockTag.getString("item")));
                assignedBlocks.add(new AssignedBlock(pos, item));
            }
        }
        assignedGolams.clear();
        ListTag assignedGolamsTag = compound.getList("assigned_golams", Tag.TAG_COMPOUND);
        for (Tag tag_ : assignedGolamsTag) {
            if (tag_ instanceof CompoundTag blockTag) {
                UUID uuid = blockTag.getUUID("uuid");
                assignedGolams.add(new AssignedGolams(uuid));
            }
        }
        this.updateGoals();
        this.readInventoryFromTag(compound, this.registryAccess());
    }



    private void setupAnimationStates() {
        if(this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = 80;
            idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.AMETHYST_BLOCK_HIT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.AMETHYST_BLOCK_BREAK;
    }

    protected @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemBySlot(EquipmentSlot.MAINHAND);
        if (player.getCooldowns().isOnCooldown(itemstack.getItem())) {
            return InteractionResult.FAIL;
        }
        if (itemstack.is(Items.REDSTONE) && player.isCreative()) {
            GolamProfessions current = getTypeVariant();
            GolamProfessions next = GolamProfessions.byId((current.getId() + 1) % GolamProfessions.values().length);
            this.setVariant(next);
            updateGoals();
            player.getCooldowns().addCooldown(itemstack.getItem(), 5);
            return InteractionResult.sidedSuccess(this.level().isClientSide);

        } else if (itemstack.is(Items.SMOOTH_BASALT)) {
            float maxH = getMaxHealth();
            float actH = getHealth();
            if (actH==maxH){
                return InteractionResult.PASS;
            } else {
                float newH = actH + repair_value_by_smooth_basalt;
                if (newH >= getMaxHealth()){
                    newH = getMaxHealth();
                }
                itemstack.consume(1, player);
                this.playSound(SoundEvents.WOLF_ARMOR_REPAIR, 0.2F, 1.0F);
                this.playSound(SoundEvents.BASALT_BREAK, 0.5F, 1.0F);
                setHealth(newH);
                player.getCooldowns().addCooldown(itemstack.getItem(), 5);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        } else if (itemstack.is(Items.AIR)) {
            ItemStack golamItem = getItemBySlot(EquipmentSlot.OFFHAND);
            if (!golamItem.isEmpty()) {
                ItemStack item = golamItem.copyWithCount(1);
                player.setItemSlot(EquipmentSlot.MAINHAND, item);
                golamItem.shrink(1);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;


        } else if (itemstack.is(Items.FILLED_MAP) && getTypeVariant()==GolamProfessions.CARTOGRAPHER) {

            ItemStack golamItem = getItemBySlot(EquipmentSlot.OFFHAND);
            if (golamItem.isEmpty()) {

                setItemSlot(EquipmentSlot.OFFHAND, itemstack.copyWithCount(1));
                itemstack.shrink(1);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }else if (itemstack.is(Items.AMETHYST_BLOCK)) {
            if (Objects.requireNonNull(getTypeVariant()) == GolamProfessions.BLACKSMITH) {
                ItemStack stack = new ItemStack(Items.AMETHYST_SHARD);
                stack.setCount(2);
                itemstack.consume(1, player);
                this.playSound(SoundEvents.UI_STONECUTTER_TAKE_RESULT, 0.5F, 1.0F);
                this.playSound(SoundEvents.AMETHYST_BLOCK_PLACE, 1.0F, 1.0F);
                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                player.getCooldowns().addCooldown(itemstack.getItem(), 5);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }/* else if (itemstack.is(Items.AMETHYST_SHARD)) {
            switch (getTypeVariant()) {
                case BLACKSMITH :
                    break;
                case CARTOGRAPHER:
                    break;
                case DELIVERER:
                    break;
                case GUARD:
                    break;
                default:
                    break;
            }
            return InteractionResult.PASS;
        } */else {
            return InteractionResult.PASS;
        }
    }

    public class AssignedBlock {
        private final BlockPos blockPos;
        private final Item item;

        public AssignedBlock(BlockPos blockPos, Item item) {
            this.blockPos = blockPos;
            this.item = item;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }

        public Item getItem() {
            return item;
        }
    }


    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player) {
            if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {

                GolamProfessions current = getTypeVariant();

                ItemStack stack = new ItemStack(ModItems.GOLAM_ITEM.get());
                String professionName = current.getProfessionName();
                stack.set(ModDataComponents.GOLAM_PROFESSION.get(), professionName);

                int customModelData = GolamProfessions.getIndex(professionName);
                stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(customModelData));

                double maxDurability = (int)this.getMaxHealth();
                double currentDurability = (int) this.getHealth();

                if (getTypeVariant() != GolamProfessions.GUARD){
                    maxDurability = maxDurability*2;
                    currentDurability = currentDurability *2;
                }
                stack.setDamageValue((int)maxDurability - (int)currentDurability);


                if (!player.getInventory().add(stack)) {
                    player.drop(stack, false);
                }
                beforeDespawn();
                this.discard();
                return true;
            }

        }
        return super.hurt(source, amount);
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return 0;
    }

    @Override
    public void setRemainingPersistentAngerTime(int i) {

    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return null;
    }

    @Override
    public void setPersistentAngerTarget(@javax.annotation.Nullable UUID target) {
    }

    @Override
    public void startPersistentAngerTimer() {

    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        this.updateGoals();
    }

    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState block) {
            this.playSound(SoundEvents.AMETHYST_BLOCK_STEP, 0.2F, 1.0F);
    }

    public @NotNull Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.1F * this.getEyeHeight(), this.getBbWidth() * 0.4F);
    }
    private float getAttackDamage() {
        return (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    public boolean doHurtTarget(Entity entity) {
        int attackAnimationTick = 10;
        this.level().broadcastEntityEvent(this, (byte)4);
        float f = this.getAttackDamage();
        float f1 = (int)f > 0 ? f / 2.0F + (float)this.random.nextInt((int)f) : f;
        DamageSource damagesource = this.damageSources().mobAttack(this);
        boolean flag = entity.hurt(damagesource, f1);
        if (flag) {
            double var10000;
            if (entity instanceof LivingEntity livingentity) {
                var10000 = livingentity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            } else {
                var10000 = 0.0;
            }

            double d0 = var10000;
            double d1 = Math.max(0.0, 1.0 - d0);
            entity.setDeltaMovement(entity.getDeltaMovement().add(0.0, 0.4000000059604645 * d1, 0.0));
            Level var11 = this.level();
            if (var11 instanceof ServerLevel serverlevel) {
                EnchantmentHelper.doPostAttackEffects(serverlevel, entity, damagesource);
            }
        }

        this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        return flag;
    }

    @Override
    public void tick() {
        super.tick();

        if(this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }

    @Nullable
    @Override
    public ItemStack getPickResult() {
        ItemStack stack = new ItemStack(ModItems.GOLAM_ITEM.get());
        String professionName = this.getTypeVariant().getProfessionName();
        stack.set(ModDataComponents.GOLAM_PROFESSION.get(), professionName);
        int customModelData = GolamProfessions.getIndex(professionName);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(customModelData));
        return stack;
    }

    @Override
    public void setItemSlotAndDropWhenKilled(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
        this.setItemSlot(slot, stack);
        this.setGuaranteedDrop(slot);
        this.persistenceRequired = true;
    }

    @Override
    public @NotNull SimpleContainer getInventory() {
        return inventory;
    }
    public @NotNull SlotAccess getSlot(int slot) {
        int i = slot - 300;
        return i >= 0 && i < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, i) : super.getSlot(slot);
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        beforeDespawn();
        super.die(damageSource);
    }

    public void beforeDespawn(){
        if (!this.level().isClientSide) {
            dropAllItems();
        }
        for (AssignedBlock ab : assignedBlocks) {
                BlockPos pos = ab.getBlockPos();
                BlockEntity be = this.level().getBlockEntity(pos);
                if (be instanceof GolamInterfaceBE interfaceBE) {
                    interfaceBE.removeAssignedGolam(this.getUUID());
                }
        }
        if (getTypeVariant()!=GolamProfessions.GUARD){
            List<GolamEntity> guards = this.level().getEntitiesOfClass(GolamEntity.class, this.getBoundingBox().inflate(50),
                    entity -> entity.getTypeVariant() == GolamProfessions.GUARD);

            for (GolamEntity guard : guards) {
                guard.removeAssignedGolam(this.getUUID());
            }
        }
    }
    private void dropAllItems() {
        if (this.level().isClientSide) return;
        SimpleContainer inventory = getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) {
                spawnAtLocation(stack);
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = this.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                spawnAtLocation(stack);
                this.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }
}
