package com.portingdeadmods.cable_facades;

import com.mojang.logging.LogUtils;
import com.portingdeadmods.cable_facades.api.CableFacadesAPI;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeType;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(CFMain.MODID)
public class CFMain {
    public static final String MODID = "cable_facades";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CFMain(IEventBus modEventBus, ModContainer modContainer) {
        registerDefaultFacadeType();
        CFItems.ITEMS.register(modEventBus);
        CFCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        CFRecipes.RECIPES.register(modEventBus);
        CFDataComponents.DATA_COMPONENTS.register(modEventBus);
        CFAttachments.ATTACHMENTS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, CFConfig.SPEC);
        CableFacadesAPI.initializeAPI();
    }

    public static final ResourceLocation DEFAULT_OUTLINE_MODEL =
            ResourceLocation.fromNamespaceAndPath(MODID, "item/facade_outline");

    private static void registerDefaultFacadeType() {
        FacadeTypes.register(new FacadeType(
                FacadeTypes.DEFAULT_ID,
                CFMain::defaultCanApply,
                DEFAULT_OUTLINE_MODEL
        ));
    }

    private static boolean defaultCanApply(BlockState state) {
        boolean hasTag = state.getTags().anyMatch(tag -> tag.equals(CFItemTags.SUPPORTS_FACADE));
        return hasTag || CFConfig.isBlockAllowed(state.getBlock());
    }

    public static boolean isIrisLoaded() {
        return ModList.get().isLoaded("iris");
    }
}
