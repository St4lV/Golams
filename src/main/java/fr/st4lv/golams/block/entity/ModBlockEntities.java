package fr.st4lv.golams.block.entity;

import fr.st4lv.golams.Golams;
import fr.st4lv.golams.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Golams.MODID);

    public static final Supplier<BlockEntityType<GolamInterfaceBE>> GOLAM_INTERFACE_BE =
            BLOCK_ENTITIES.register("golam_interface_be", () -> BlockEntityType.Builder.of(
                    GolamInterfaceBE::new, ModBlocks.GOLAM_INTERFACE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
