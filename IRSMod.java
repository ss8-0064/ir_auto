package com.irs.signals;

import com.irs.signals.block.SignalBlock;
import com.irs.signals.block.SignalTileEntity;
import com.irs.signals.item.SignalLinkerItem;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

@Mod(modid = IRSMod.MODID, name = IRSMod.NAME, version = IRSMod.VERSION,
        dependencies = "required-after:immersiverailroading;required-after:forge@[14.23.5.2847,)")
public class IRSMod {
    public static final String MODID = "irs_signals";
    public static final String NAME = "Immersive Railroading Signals";
    public static final String VERSION = "1.0.0";

    public static Logger logger;

    @Instance(MODID)
    public static IRSMod instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("IRS Signals: PreInit - registering TileEntity");
        GameRegistry.registerTileEntity(SignalTileEntity.class, new ResourceLocation(MODID, "signal"));
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("IRS Signals: Init complete");
    }

    @Mod.EventBusSubscriber(modid = MODID)
    public static class RegistryHandler {
        @SubscribeEvent
        public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
            SignalBlock signal = new SignalBlock();
            ModBlocks.SIGNAL = signal;
            event.getRegistry().register(signal);
            IRSMod.logger.info("IRS Signals: Registered block {}", signal.getRegistryName());
        }

        @SubscribeEvent
        public static void onRegisterItems(RegistryEvent.Register<Item> event) {
            ItemBlock signalItem = new ItemBlock(ModBlocks.SIGNAL);
            signalItem.setRegistryName(ModBlocks.SIGNAL.getRegistryName());
            event.getRegistry().register(signalItem);
            ModItems.SIGNAL_ITEM = signalItem;

            SignalLinkerItem linker = new SignalLinkerItem();
            event.getRegistry().register(linker);
            ModItems.SIGNAL_LINKER = linker;
            IRSMod.logger.info("IRS Signals: Registered items");
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Side.CLIENT)
    public static class ModelRegistryHandler {
        @SubscribeEvent
        @SideOnly(Side.CLIENT)
        public static void onModelRegister(ModelRegistryEvent event) {
            registerModel(Item.getItemFromBlock(ModBlocks.SIGNAL));
            registerModel(ModItems.SIGNAL_LINKER);
        }

        @SideOnly(Side.CLIENT)
        private static void registerModel(Item item) {
            ModelLoader.setCustomModelResourceLocation(item, 0,
                    new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}
