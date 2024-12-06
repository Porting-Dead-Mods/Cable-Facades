package com.portingdeadmods.cable_facades;

import com.mojang.logging.LogUtils;
import com.portingdeadmods.cable_facades.networking.CFMessages;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.registries.CFRecipes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CFMain.MODID)
public class CFMain {
    public static final String MODID = "cable_facades";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CFMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        CFItems.ITEMS.register(modEventBus);
        //CFCreativeTabs.CREATIVE_MODE_TAB.register(modEventBus);
        CFRecipes.RECIPES.register(modEventBus);
        CFMessages.register();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CFConfig.SPEC);

    }

}
