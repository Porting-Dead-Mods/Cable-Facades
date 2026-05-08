package com.portingdeadmods.cable_facades;

import com.mojang.logging.LogUtils;
import com.portingdeadmods.cable_facades.api.CableFacadesAPI;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeType;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.registries.*;
import net.minecraft.resources.Identifier;
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

    public static final Identifier DEFAULT_OUTLINE_MODEL =
            Identifier.fromNamespaceAndPath(MODID, "item/facade_outline");

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
        return state.is(CFItemTags.SUPPORTS_FACADE) || CFConfig.isBlockAllowed(state.getBlock());
    }

    public static boolean isIrisLoaded() {
        return ModList.get().isLoaded("iris");
    }
}
