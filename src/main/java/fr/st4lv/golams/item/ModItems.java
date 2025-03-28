package fr.st4lv.golams.item;

import fr.st4lv.golams.Golams;
import fr.st4lv.golams.data_component.ModDataComponents;
import fr.st4lv.golams.item.custom.GolamCore;
import fr.st4lv.golams.item.custom.GolamItem;
import fr.st4lv.golams.trim.ModSmithingTemplatesItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Golams.MODID);

    //GOLAM

    public static final DeferredItem<Item> GOLAM_ITEM = ITEMS.register("golam",
            () -> new GolamItem(new Item.Properties()
                    .component(ModDataComponents.GOLAM_PROFESSION.value(), "unassigned")
                    .durability(20)
                ));
    //GOLAM PROFESSIONS TEMPLATES

    public static final DeferredItem<Item> GOLAM_UPGRADE_TEMPLATE = ITEMS.register("golam_upgrade_template",
            ModSmithingTemplatesItem::createGolamTemplatesUpgradeTemplate);

    public static final DeferredItem<Item> GOLAM_BLACKSMITH_UPGRADE_TEMPLATE = ITEMS.register("golam_blacksmith_upgrade_template",
            ModSmithingTemplatesItem::createGolamProfessionUpgradeTemplate);

    public static final DeferredItem<Item> GOLAM_CARTOGRAPHER_UPGRADE_TEMPLATE = ITEMS.register("golam_cartographer_upgrade_template",
            ModSmithingTemplatesItem::createGolamProfessionUpgradeTemplate);
    public static final DeferredItem<Item> GOLAM_DELIVERER_UPGRADE_TEMPLATE = ITEMS.register("golam_deliverer_upgrade_template",
            ModSmithingTemplatesItem::createGolamProfessionUpgradeTemplate);
    public static final DeferredItem<Item> GOLAM_GUARD_UPGRADE_TEMPLATE = ITEMS.register("golam_guard_upgrade_template",
            ModSmithingTemplatesItem::createGolamProfessionUpgradeTemplate);
    public static final DeferredItem<Item> GOLAM_HARVESTER_UPGRADE_TEMPLATE = ITEMS.register("golam_harvester_upgrade_template",
            ModSmithingTemplatesItem::createGolamProfessionUpgradeTemplate);


    //ITEMS

    public static final DeferredItem<Item> GOLAM_CORE = ITEMS.register("golam_core",
            () -> new GolamCore(new Item.Properties()
                    .durability(1)
            ));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
