package com.portingdeadmods.cable_facades.compat;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import com.portingdeadmods.cable_facades.registries.CFItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Blocks;

import java.util.Collections;
import java.util.Optional;

@JeiPlugin
public class CFJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ItemStack ingredientStack = new ItemStack(Blocks.COBBLESTONE);
        ingredientStack.set(DataComponents.CUSTOM_NAME, Component.translatable("cable_facades.jei.empty").withStyle(ChatFormatting.RESET));

        ItemStack facadeResult = new ItemStack(CFItems.FACADE.get());
        facadeResult.set(CFDataComponents.FACADE_BLOCK, Optional.of(Blocks.COBBLESTONE));
        facadeResult.set(DataComponents.CUSTOM_NAME, Component.translatable("cable_facades.jei.facade_empty").withStyle(ChatFormatting.RESET));

        registration.addRecipes(RecipeTypes.CRAFTING, Collections.singletonList(new RecipeHolder<>(
                ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "facade_crafting"),
                new ShapelessRecipe("facades", CraftingBookCategory.BUILDING, facadeResult,
                        NonNullList.of(Ingredient.EMPTY, Ingredient.of(CFItems.FACADE.get()), Ingredient.of(ingredientStack)))
        )));

        ItemStack directionalResult = new ItemStack(CFItems.DIRECTIONAL_FACADE.get());
        directionalResult.set(CFDataComponents.FACADE_BLOCK, Optional.of(Blocks.COBBLESTONE));

        registration.addRecipes(RecipeTypes.CRAFTING, Collections.singletonList(new RecipeHolder<>(
                ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "directional_facade_crafting"),
                new ShapelessRecipe("facades", CraftingBookCategory.BUILDING, directionalResult,
                        NonNullList.of(Ingredient.EMPTY, Ingredient.of(CFItems.DIRECTIONAL_FACADE.get()), Ingredient.of(ingredientStack)))
        )));

        ItemStack conversionResult = new ItemStack(CFItems.DIRECTIONAL_FACADE.get(), 6);
        conversionResult.set(CFDataComponents.FACADE_BLOCK, Optional.of(Blocks.COBBLESTONE));
        ItemStack conversionInput = new ItemStack(CFItems.FACADE.get());
        conversionInput.set(CFDataComponents.FACADE_BLOCK, Optional.of(Blocks.COBBLESTONE));

        registration.addRecipes(RecipeTypes.CRAFTING, Collections.singletonList(new RecipeHolder<>(
                ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "facade_to_directional"),
                new ShapelessRecipe("facades", CraftingBookCategory.BUILDING, conversionResult,
                        NonNullList.of(Ingredient.EMPTY, Ingredient.of(conversionInput)))
        )));
    }
}
