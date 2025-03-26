package fr.st4lv.golams.item.custom;

import fr.st4lv.golams.Golams;
import fr.st4lv.golams.data_component.ModDataComponents;
import fr.st4lv.golams.entity.GolamProfessions;
import fr.st4lv.golams.entity.ModEntities;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GolamItem extends Item {

    public GolamItem(Properties properties) {
        super(properties);
    }
    private static final Map<String, ResourceLocation> MODEL_MAP = new HashMap<>();

    static {
        for (GolamProfessions profession : GolamProfessions.values()) {
            MODEL_MAP.put(profession.getProfessionName(),
                    ResourceLocation.fromNamespaceAndPath(Golams.MODID, "item/" + profession.getProfessionName() + "_golam"));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {

        if (stack.has(ModDataComponents.GOLAM_PROFESSION.get())) {
            String golamProfession = stack.get(ModDataComponents.GOLAM_PROFESSION.get());
            tooltipComponents.add(Component.translatable("tooltip.golams.golam_profession", golamProfession));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!level.isClientSide) {
            ItemStack stack = context.getItemInHand();
            Player player = context.getPlayer();
            BlockPos pos = context.getClickedPos().relative(context.getClickedFace());

            GolamEntity golam = new GolamEntity(ModEntities.GOLAM.get(), level);
            golam.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360F, 0);

            if (stack.has(ModDataComponents.GOLAM_PROFESSION)) {
                String professionName = stack.get(ModDataComponents.GOLAM_PROFESSION);
                golam.setVariant(GolamProfessions.byName(professionName));
            }
            golam.updateGoals();
            double actH = stack.getMaxDamage()- stack.getDamageValue();
            if (!Objects.equals(stack.get(ModDataComponents.GOLAM_PROFESSION), "guard")){
                actH = actH*0.5;
            }
            golam.setHealth((int) actH);
            level.addFreshEntity(golam);
            assert player != null;
            player.getInventory().removeItem(stack);


            return InteractionResult.SUCCESS;
        }

        return InteractionResult.CONSUME;
    }

}
