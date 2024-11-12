package com.portingdeadmods.cable_facades;

import com.mojang.logging.LogUtils;
import com.portingdeadmods.cable_facades.registries.CFBlocks;
import com.portingdeadmods.cable_facades.registries.CFCreativeTabs;
import com.portingdeadmods.cable_facades.registries.CFItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CFMain.MODID)
public class CFMain {

    public static final String MODID = "cable_facades";
    private static final Logger LOGGER = LogUtils.getLogger();


    public CFMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        CFBlocks.BLOCKS.register(modEventBus);
        CFItems.ITEMS.register(modEventBus);
        CFCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CFConfig.SPEC);
    }

}
