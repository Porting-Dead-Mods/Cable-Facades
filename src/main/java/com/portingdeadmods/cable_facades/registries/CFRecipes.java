package com.portingdeadmods.cable_facades.registries;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.content.recipes.FacadeCraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CFRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister
            .create(ForgeRegistries.RECIPE_SERIALIZERS, CFMain.MODID);

    public static final RegistryObject<RecipeSerializer<FacadeCraftingRecipe>> FACADE = RECIPES.register("facade",
            () -> new FacadeRecipeSerializer<>(FacadeCraftingRecipe::new));
}
