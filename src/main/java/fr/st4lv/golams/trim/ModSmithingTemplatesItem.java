package fr.st4lv.golams.trim;

import fr.st4lv.golams.Golams;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.item.SmithingTemplateItem;

import java.util.List;

public class ModSmithingTemplatesItem extends SmithingTemplateItem {
    private static final ChatFormatting TITLE_FORMAT;
    private static final ChatFormatting DESCRIPTION_FORMAT;
    private static final Component GOLAM_UPGRADE;
    private static final Component GOLAM_TEMPLATE_UPGRADE;
    private static final Component GOLAM_UPGRADE_TEMPLATE_APPLIES_TO;
    private static final Component GOLAM_UPGRADE_APPLIES_TO;
    private static final Component GOLAM_UPGRADE_INGREDIENTS;
    private static final Component GOLAM_UPGRADE_BASE_SLOT_DESCRIPTION;
    private static final Component GOLAM_UPGRADE_ADDITIONS_SLOT_DESCRIPTION;
    private static final ResourceLocation EMPTY_SLOT_BLOCK;
    private static final ResourceLocation EMPTY_SLOT_SPYGLASS;
    private static final ResourceLocation EMPTY_SLOT_GOLAM;
    private static final ResourceLocation EMPTY_SLOT_AMETHYST_SHARD;
    public ModSmithingTemplatesItem(Component appliesTo, Component ingredients, Component upgradeDescription, Component baseSlotDescription, Component additionsSlotDescription, List<ResourceLocation> baseSlotEmptyIcons, List<ResourceLocation> additionalSlotEmptyIcons, FeatureFlag... requiredFeatures) {
        super(appliesTo, ingredients, upgradeDescription, baseSlotDescription, additionsSlotDescription, baseSlotEmptyIcons, additionalSlotEmptyIcons, requiredFeatures);
    }

    private static List<ResourceLocation> createGolamTemplatesUpgradeIconList() {
        return List.of(EMPTY_SLOT_BLOCK, EMPTY_SLOT_SPYGLASS, EMPTY_SLOT_GOLAM);
    }

    private static List<ResourceLocation> createGolamProfessionTemplateUpgradeIconList() {
        return List.of( EMPTY_SLOT_GOLAM);
    }

    private static List<ResourceLocation> createGolamUpgradeMaterialList() {
        return List.of(EMPTY_SLOT_AMETHYST_SHARD);
    }

    public static SmithingTemplateItem createGolamTemplatesUpgradeTemplate() {
        return new SmithingTemplateItem(GOLAM_UPGRADE_TEMPLATE_APPLIES_TO, GOLAM_UPGRADE_INGREDIENTS, GOLAM_TEMPLATE_UPGRADE, GOLAM_UPGRADE_BASE_SLOT_DESCRIPTION, GOLAM_UPGRADE_ADDITIONS_SLOT_DESCRIPTION, createGolamTemplatesUpgradeIconList(), createGolamUpgradeMaterialList(), new FeatureFlag[0]);
    }
    public static SmithingTemplateItem createGolamProfessionUpgradeTemplate() {
        return new SmithingTemplateItem(GOLAM_UPGRADE_APPLIES_TO, GOLAM_UPGRADE_INGREDIENTS, GOLAM_UPGRADE, GOLAM_UPGRADE_BASE_SLOT_DESCRIPTION, GOLAM_UPGRADE_ADDITIONS_SLOT_DESCRIPTION, createGolamProfessionTemplateUpgradeIconList(), createGolamUpgradeMaterialList(), new FeatureFlag[0]);
    }


    static {
        TITLE_FORMAT = ChatFormatting.GRAY;
        DESCRIPTION_FORMAT = ChatFormatting.BLUE;
        GOLAM_TEMPLATE_UPGRADE = Component.translatable(Util.makeDescriptionId("upgrade", ResourceLocation.fromNamespaceAndPath(Golams.MODID,"golam_upgrade_template"))).withStyle(TITLE_FORMAT);
        GOLAM_UPGRADE = Component.translatable(Util.makeDescriptionId("upgrade", ResourceLocation.fromNamespaceAndPath(Golams.MODID,"golam_profession_upgrade"))).withStyle(TITLE_FORMAT);

        GOLAM_UPGRADE_TEMPLATE_APPLIES_TO = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(Golams.MODID,"template_upgrade_professions_items"))).withStyle(DESCRIPTION_FORMAT);
        GOLAM_UPGRADE_APPLIES_TO = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(Golams.MODID,"golam"))).withStyle(DESCRIPTION_FORMAT);
        GOLAM_UPGRADE_INGREDIENTS = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("amethyst_shard"))).withStyle(DESCRIPTION_FORMAT);
        GOLAM_UPGRADE_BASE_SLOT_DESCRIPTION = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.netherite_upgrade.base_slot_description")));
        GOLAM_UPGRADE_ADDITIONS_SLOT_DESCRIPTION = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.withDefaultNamespace("smithing_template.netherite_upgrade.additions_slot_description")));
        EMPTY_SLOT_GOLAM = ResourceLocation.withDefaultNamespace("item/empty_slot_amethyst_shard");
        EMPTY_SLOT_SPYGLASS = ResourceLocation.withDefaultNamespace("item/empty_slot_amethyst_shard");
        EMPTY_SLOT_BLOCK = ResourceLocation.withDefaultNamespace("item/empty_slot_amethyst_shard");
        EMPTY_SLOT_AMETHYST_SHARD = ResourceLocation.fromNamespaceAndPath("minecraft","item/empty_slot_amethyst_shard");
    }
}
