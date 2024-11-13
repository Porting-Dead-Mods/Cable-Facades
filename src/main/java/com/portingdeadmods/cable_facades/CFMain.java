package com.portingdeadmods.cable_facades;

import com.mojang.logging.LogUtils;
import com.portingdeadmods.cable_facades.content.recipes.FacadeCraftingRecipe;
import com.portingdeadmods.cable_facades.networking.ModMessages;
import com.portingdeadmods.cable_facades.registries.CFBlocks;
import com.portingdeadmods.cable_facades.registries.CFCreativeTabs;
import com.portingdeadmods.cable_facades.registries.CFItems;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(CFMain.MODID)
public class CFMain {
    public static final String MODID = "cable_facades";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister
            .create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);

    public static final RegistryObject<RecipeSerializer<?>> FACADE = RECIPES.register("facade",
            () -> new SimpleCraftingRecipeSerializer<>(FacadeCraftingRecipe::new));

    public CFMain() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        CFBlocks.BLOCKS.register(modEventBus);
        CFItems.ITEMS.register(modEventBus);
        CFCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        RECIPES.register(modEventBus);
        ModMessages.register();

        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CFConfig.SPEC);
    }

}
