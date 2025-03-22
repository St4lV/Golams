package fr.st4lv.golams.item;

import fr.st4lv.golams.Golams;
import fr.st4lv.golams.block.ModBlocks;
import fr.st4lv.golams.data_component.ModDataComponents;
import fr.st4lv.golams.entity.GolamProfessions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Golams.MODID);

    public static final Supplier<CreativeModeTab> GOLAMS_ITEMS_TAB = CREATIVE_MODE_TAB.register("golams_creative_tab_items",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.GOLAM_ITEM.get()))
                    .title(Component.translatable("creativetab.golams.creative_tab_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        int index = 0;
                        for (GolamProfessions profession : GolamProfessions.values()) {
                            ItemStack stack = new ItemStack(ModItems.GOLAM_ITEM.get());
                            stack.set(ModDataComponents.GOLAM_PROFESSION.get(), profession.getProfessionName());
                            stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(profession.getId()));
                            output.accept(stack);
                            index += 1;
                        }
                        output.accept(ModItems.GOLAM_CORE.get());
                        output.accept(ModItems.GOLAM_UPGRADE_TEMPLATE.get());
                        output.accept(ModItems.GOLAM_BLACKSMITH_UPGRADE_TEMPLATE.get());
                        output.accept(ModItems.GOLAM_CARTOGRAPHER_UPGRADE_TEMPLATE.get());
                        output.accept(ModItems.GOLAM_DELIVERER_UPGRADE_TEMPLATE.get());
                        output.accept(ModItems.GOLAM_GUARD_UPGRADE_TEMPLATE.get());
                        output.accept(ModBlocks.GOLAM_INTERFACE);
                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
