package com.portingdeadmods.cable_facades.compat;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.registries.CFItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Blocks;

import java.util.Collections;

@JeiPlugin
public class CFJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation(CFMain.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ItemStack itemStack = new ItemStack(CFItems.FACADE.get());
        itemStack.getOrCreateTag().putString(FacadeItem.FACADE_BLOCK, BuiltInRegistries.BLOCK.getKey(Blocks.COBBLESTONE).toString());
        itemStack.setHoverName(Component.literal("Facade - Any Block").withStyle(ChatFormatting.RESET));

        ItemStack ingredientStack = new ItemStack(Blocks.COBBLESTONE);
        ingredientStack.setHoverName(Component.literal("Any Block").withStyle(ChatFormatting.RESET));

        registration.addRecipes(RecipeTypes.CRAFTING, Collections.singletonList(new ShapelessRecipe(
                new ResourceLocation(CFMain.MODID, "facade_crafting"),
                "facades",
                CraftingBookCategory.BUILDING,
                itemStack,
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(CFItems.FACADE.get()), Ingredient.of(ingredientStack))
        )));
    }
}
