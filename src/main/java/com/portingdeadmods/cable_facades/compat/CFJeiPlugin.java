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
        ItemStack itemStack = new ItemStack(CFItems.FACADE.get());
        itemStack.set(CFDataComponents.FACADE_BLOCK, Optional.of(Blocks.COBBLESTONE));
        itemStack.set(DataComponents.CUSTOM_NAME, Component.literal("Facade - Any Block").withStyle(ChatFormatting.RESET));

        ItemStack ingredientStack = new ItemStack(Blocks.COBBLESTONE);
        ingredientStack.set(DataComponents.CUSTOM_NAME, Component.literal("Any Block").withStyle(ChatFormatting.RESET));

        registration.addRecipes(RecipeTypes.CRAFTING, Collections.singletonList(new RecipeHolder<>(
                ResourceLocation.fromNamespaceAndPath(CFMain.MODID, "facade_crafting"),
                new ShapelessRecipe(
                        "facades",
                        CraftingBookCategory.BUILDING,
                        itemStack,
                        NonNullList.of(Ingredient.EMPTY, Ingredient.of(CFItems.FACADE.get()), Ingredient.of(ingredientStack))
                )
        )));
    }
}
