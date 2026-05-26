package com.portingdeadmods.cable_facades.compat;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.utils.FacadeItemNbt;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
        ItemStack ingredientStack = new ItemStack(Blocks.COBBLESTONE);
        ingredientStack.setHoverName(new TranslatableComponent("cable_facades.jei.any_block").withStyle(ChatFormatting.RESET));

        ItemStack facadeResult = new ItemStack(CFItems.FACADE.get());
        FacadeItemNbt.setFacadeBlock(facadeResult, Blocks.COBBLESTONE);
        facadeResult.setHoverName(new TranslatableComponent("cable_facades.jei.facade_any_block").withStyle(ChatFormatting.RESET));

        registration.addRecipes(RecipeTypes.CRAFTING, Collections.singletonList(new ShapelessRecipe(
                new ResourceLocation(CFMain.MODID, "facade_crafting"),
                "facades",
                facadeResult,
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(CFItems.FACADE.get()), Ingredient.of(ingredientStack))
        )));

        ItemStack directionalResult = new ItemStack(CFItems.DIRECTIONAL_FACADE.get());
        FacadeItemNbt.setFacadeBlock(directionalResult, Blocks.COBBLESTONE);

        registration.addRecipes(RecipeTypes.CRAFTING, Collections.singletonList(new ShapelessRecipe(
                new ResourceLocation(CFMain.MODID, "directional_facade_crafting"),
                "facades",
                directionalResult,
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(CFItems.DIRECTIONAL_FACADE.get()), Ingredient.of(ingredientStack))
        )));
    }
}
