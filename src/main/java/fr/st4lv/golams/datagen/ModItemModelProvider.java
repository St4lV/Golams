package fr.st4lv.golams.datagen;

import fr.st4lv.golams.Golams;
import fr.st4lv.golams.entity.GolamProfessions;
import fr.st4lv.golams.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;


public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Golams.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ItemModelBuilder builder = getBuilder("golam")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", "golams:item/golam_item");

        for (GolamProfessions profession : GolamProfessions.values()) {
            builder.override()
                    .predicate(ResourceLocation.withDefaultNamespace("custom_model_data"), profession.getId())
                    .model(getExistingFile(modLoc("item/" + profession.getProfessionName() + "_golam")));
        }
        basicItem(ModItems.GOLAM_UPGRADE_TEMPLATE.get());
        basicItem(ModItems.GOLAM_BLACKSMITH_UPGRADE_TEMPLATE.get());
        basicItem(ModItems.GOLAM_CARTOGRAPHER_UPGRADE_TEMPLATE.get());
        basicItem(ModItems.GOLAM_DELIVERER_UPGRADE_TEMPLATE.get());
        basicItem(ModItems.GOLAM_GUARD_UPGRADE_TEMPLATE.get());
        basicItem(ModItems.GOLAM_HARVESTER_UPGRADE_TEMPLATE.get());

        basicItem(ModItems.GOLAM_CORE.get());

    }

    private ItemModelBuilder handheldItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(Golams.MODID,"item/" + item.getId().getPath()));
    }
}
