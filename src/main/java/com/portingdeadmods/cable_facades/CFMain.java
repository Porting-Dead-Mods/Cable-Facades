package com.portingdeadmods.cable_facades;

import com.mojang.logging.LogUtils;
import com.portingdeadmods.cable_facades.registries.CFCreativeTabs;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.registries.CFRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(CFMain.MODID)
public class CFMain {
    public static final String MODID = "cable_facades";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CFMain(IEventBus modEventBus, ModContainer modContainer) {
        CFItems.ITEMS.register(modEventBus);
        CFCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        CFRecipes.RECIPES.register(modEventBus);
        CFDataComponents.DATA_COMPONENTS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, CFConfig.SPEC);

    }

}
