package com.portingdeadmods.cable_facades.registries;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.content.items.DirectionalFacadeItem;
import com.portingdeadmods.cable_facades.content.recipes.DirectionalFacadeCraftingRecipe;
import com.portingdeadmods.cable_facades.content.recipes.FacadeCraftingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CFRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister
            .create(BuiltInRegistries.RECIPE_SERIALIZER, CFMain.MODID);

    public static final Supplier<RecipeSerializer<FacadeCraftingRecipe>> FACADE = RECIPES.register("facade",
            () -> FacadeCraftingRecipe.SERIALIZER);

    public static final Supplier<RecipeSerializer<DirectionalFacadeCraftingRecipe>> DIRECTIONAL_FACADE = RECIPES.register("directional_facade",
            () -> DirectionalFacadeCraftingRecipe.SERIALIZER);
}
