package com.portingdeadmods.cable_facades.registries;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.content.recipes.FacadeCraftingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CFRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister
            .create(BuiltInRegistries.RECIPE_SERIALIZER, CFMain.MODID);

    public static final Supplier<RecipeSerializer<?>> FACADE = RECIPES.register("facade",
            () -> new SimpleCraftingRecipeSerializer<>(FacadeCraftingRecipe::new));
}
