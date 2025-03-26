package fr.st4lv.golams;

import fr.st4lv.golams.block.ModBlocks;
import fr.st4lv.golams.block.entity.ModBlockEntities;
import fr.st4lv.golams.block.entity.renderer.GolamInterfaceBERenderer;
import fr.st4lv.golams.data_component.ModDataComponents;
import fr.st4lv.golams.entity.ModEntities;

import fr.st4lv.golams.entity.client.GolamRenderer;

import fr.st4lv.golams.item.ModCreativeModeTabs;
import fr.st4lv.golams.item.ModItems;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Golams.MODID)
public class Golams {

    /**
     *  Big thanks to @ModdingByKaupenjoe on YouTube for the tutorials, qnd to other peoples of Internet for the codes parts/hints/tips on posts
     *
     *
    **/

    public static final String MODID = "golams";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Golams(IEventBus modEventBus, ModContainer modContainer) {

        ModLoadingContext modLoadingContext = ModLoadingContext.get();

        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);


        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModEntities.register(modEventBus);


        ModCreativeModeTabs.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event){

    }


    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.GOLAM_UPGRADE_TEMPLATE);
            event.accept(ModItems.GOLAM_BLACKSMITH_UPGRADE_TEMPLATE);
            event.accept(ModItems.GOLAM_CARTOGRAPHER_UPGRADE_TEMPLATE);
            event.accept(ModItems.GOLAM_DELIVERER_UPGRADE_TEMPLATE);
            event.accept(ModItems.GOLAM_GUARD_UPGRADE_TEMPLATE);
        }
        if(event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(ModBlocks.GOLAM_INTERFACE);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.GOLAM.get(), GolamRenderer::new);
        }

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.GOLAM_INTERFACE_BE.get(), GolamInterfaceBERenderer::new);
        }
    }
}