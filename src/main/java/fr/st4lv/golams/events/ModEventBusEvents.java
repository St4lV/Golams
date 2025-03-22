package fr.st4lv.golams.events;

import fr.st4lv.golams.Golams;
import fr.st4lv.golams.entity.ModEntities;
import fr.st4lv.golams.entity.client.*;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = Golams.MODID,bus = EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(GolamModel.LAYER_LOCATION, GolamModel::createBodyLayer);
        event.registerLayerDefinition(BlacksmithGolamModel.LAYER_LOCATION, BlacksmithGolamModel::createBodyLayer);
        event.registerLayerDefinition(CartographerGolamModel.LAYER_LOCATION, CartographerGolamModel::createBodyLayer);
        event.registerLayerDefinition(DelivererGolamModel.LAYER_LOCATION, DelivererGolamModel::createBodyLayer);
        event.registerLayerDefinition(GuardGolamModel.LAYER_LOCATION, GuardGolamModel::createBodyLayer);
    }
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.GOLAM.get(), GolamEntity.createAttributes().build());
    }
}
