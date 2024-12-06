package com.portingdeadmods.cable_facades.registries;

import com.google.gson.JsonObject;
import com.portingdeadmods.cable_facades.content.recipes.FacadeCraftingRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class FacadeRecipeSerializer<T extends FacadeCraftingRecipe> implements RecipeSerializer<T> {
    private final RecipeFactory<T> factory;

    public FacadeRecipeSerializer(RecipeFactory<T> factory) {
        this.factory = factory;
    }

    @Override
    public T fromJson(ResourceLocation recipeId, JsonObject json) {
        return factory.create(recipeId);
    }

    @Override
    public T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        return factory.create(recipeId);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, T recipe) {
    }

    public interface RecipeFactory<T extends FacadeCraftingRecipe> {
        T create(ResourceLocation id);
    }
}
