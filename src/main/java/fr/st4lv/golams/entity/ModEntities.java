package fr.st4lv.golams.entity;

import fr.st4lv.golams.Golams;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Golams.MODID);

    public static final Supplier<EntityType<GolamEntity>> GOLAM =
            ENTITY_TYPES.register("golam", () -> EntityType.Builder.of(GolamEntity::new, MobCategory.CREATURE)
                    .sized(0.40f, 0.9f).build("golam"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
