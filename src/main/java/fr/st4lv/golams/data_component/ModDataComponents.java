package fr.st4lv.golams.data_component;

import com.mojang.serialization.Codec;
import fr.st4lv.golams.Golams;
import fr.st4lv.golams.entity.custom.GolamEntity;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Golams.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> GOLAM_PROFESSION = register(
            "golam_profession",
            builder -> builder.persistent(Codec.STRING)
    );
    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(
            String name, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return DATA_COMPONENT_TYPES.register(name, () -> builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
