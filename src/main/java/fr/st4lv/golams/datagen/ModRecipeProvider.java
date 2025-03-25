package fr.st4lv.golams.datagen;

import fr.st4lv.golams.block.ModBlocks;
import fr.st4lv.golams.item.ModItems;
import net.minecraft.core.HolderLookup;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        generateSmithingRecipes(recipeOutput);


    }
    private void generateSmithingRecipes(RecipeOutput recipeOutput) {


        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.SMOOTH_BASALT),
                        Ingredient.of(Items.CALCITE),
                        Ingredient.of(Items.AMETHYST_SHARD),
                        RecipeCategory.TOOLS,
                        ModItems.GOLAM_CORE.get()
                ).unlocks("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("golams", "golam_core"));

        //TEMPLATE

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.SMOOTH_BASALT),
                        Ingredient.of(ModItems.GOLAM_CORE),
                        Ingredient.of(Items.AMETHYST_SHARD),
                        RecipeCategory.TOOLS,
                        ModItems.GOLAM_UPGRADE_TEMPLATE.get()
                ).unlocks("has_golam_core", has(ModItems.GOLAM_CORE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("golams", "golam_template_upgrade_smithing"));

        //PROFESSION TEMPLATES: SMITHING

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.GOLAM_UPGRADE_TEMPLATE.get()),
                        Ingredient.of(Items.SMITHING_TABLE),
                        Ingredient.of(Items.AMETHYST_SHARD),
                        RecipeCategory.TOOLS,
                        ModItems.GOLAM_BLACKSMITH_UPGRADE_TEMPLATE.get()
                )
                .unlocks("has_golam_upgrade_template", has(ModItems.GOLAM_UPGRADE_TEMPLATE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("golams", "golam_blacksmith_template_upgrade_smithing"));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.GOLAM_UPGRADE_TEMPLATE.get()),
                        Ingredient.of(Items.SPYGLASS),
                        Ingredient.of(Items.AMETHYST_SHARD),
                        RecipeCategory.TOOLS,
                        ModItems.GOLAM_CARTOGRAPHER_UPGRADE_TEMPLATE.get()
                ).unlocks("has_golam_upgrade_template", has(ModItems.GOLAM_UPGRADE_TEMPLATE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("golams", "golam_cartographer_template_upgrade_smithing"));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.GOLAM_UPGRADE_TEMPLATE.get()),
                        Ingredient.of(Items.CHEST),
                        Ingredient.of(Items.AMETHYST_SHARD),
                        RecipeCategory.TOOLS,
                        ModItems.GOLAM_DELIVERER_UPGRADE_TEMPLATE.get()
                ).unlocks("has_golam_upgrade_template", has(ModItems.GOLAM_UPGRADE_TEMPLATE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("golams", "golam_deliverer_template_upgrade_smithing"));

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ModItems.GOLAM_UPGRADE_TEMPLATE.get()),
                        Ingredient.of(Items.SHIELD),
                        Ingredient.of(Items.AMETHYST_SHARD),
                        RecipeCategory.TOOLS,
                        ModItems.GOLAM_GUARD_UPGRADE_TEMPLATE.get()
                ).unlocks("has_golam_upgrade_template", has(ModItems.GOLAM_UPGRADE_TEMPLATE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("golams", "golam_guard_template_upgrade_smithing"));

        // GOLAM INTERFACE

        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(Items.ITEM_FRAME),
                        Ingredient.of(ModItems.GOLAM_CORE),
                        Ingredient.of(Items.REDSTONE),
                        RecipeCategory.REDSTONE,
                        ModBlocks.GOLAM_INTERFACE.asItem()
                ).unlocks("has_golam_core", has(ModItems.GOLAM_CORE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath("golams", "golam_interface"));


    }


}
