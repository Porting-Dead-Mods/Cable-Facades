package com.portingdeadmods.cable_facades;

import com.mojang.logging.LogUtils;
import com.portingdeadmods.cable_facades.api.CableFacadesAPI;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeType;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.registries.CFCreativeTabs;
import com.portingdeadmods.cable_facades.registries.CFItemTags;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.registries.CFRecipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CFMain.MODID)
public class CFMain {
    public static final String MODID = "cable_facades";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation DEFAULT_OUTLINE_MODEL = new ResourceLocation(MODID, "facade_outline");

    public CFMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        registerDefaultFacadeType();

        CFItems.ITEMS.register(modEventBus);
        CFCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        CFRecipes.RECIPES.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CFConfig.SPEC);

        CableFacadesAPI.initializeAPI();
    }

    private static void registerDefaultFacadeType() {
        FacadeTypes.register(new FacadeType(
                FacadeTypes.DEFAULT_ID,
                CFMain::defaultCanApply,
                DEFAULT_OUTLINE_MODEL,
                CFItems.FACADE::get,
                CFItems.DIRECTIONAL_FACADE::get
        ));
    }

    private static boolean defaultCanApply(BlockState state) {
        boolean hasTag = state.getTags().anyMatch(tag -> tag.equals(CFItemTags.SUPPORTS_FACADE));
        return hasTag || CFConfig.isBlockAllowed(state.getBlock());
    }

    public static boolean isOculusLoaded() {
        return ModList.get().isLoaded("oculus");
    }
}
