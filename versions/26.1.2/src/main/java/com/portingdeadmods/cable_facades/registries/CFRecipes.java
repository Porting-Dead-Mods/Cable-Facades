package com.portingdeadmods.cable_facades.registries;

import com.mojang.serialization.MapCodec;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.content.recipes.DirectionalFacadeCraftingRecipe;
import com.portingdeadmods.cable_facades.content.recipes.FacadeCraftingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CFRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister
            .create(BuiltInRegistries.RECIPE_SERIALIZER, CFMain.MODID);

    private static final StreamCodec<RegistryFriendlyByteBuf, FacadeCraftingRecipe> FACADE_STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> {
            },
            buf -> new FacadeCraftingRecipe()
    );

    private static final StreamCodec<RegistryFriendlyByteBuf, DirectionalFacadeCraftingRecipe> DIRECTIONAL_FACADE_STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> {
            },
            buf -> new DirectionalFacadeCraftingRecipe()
    );

    public static final Supplier<RecipeSerializer<FacadeCraftingRecipe>> FACADE = RECIPES.register("facade",
            () -> new RecipeSerializer<>(
                    MapCodec.unit(FacadeCraftingRecipe::new),
                    FACADE_STREAM_CODEC
            ));

    public static final Supplier<RecipeSerializer<DirectionalFacadeCraftingRecipe>> DIRECTIONAL_FACADE = RECIPES.register("directional_facade",
            () -> new RecipeSerializer<>(
                    MapCodec.unit(DirectionalFacadeCraftingRecipe::new),
                    DIRECTIONAL_FACADE_STREAM_CODEC
            ));
}
