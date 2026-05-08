package com.portingdeadmods.cable_facades.compat;

import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import com.portingdeadmods.cable_facades.registries.CFItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@JeiPlugin
public class CFJeiPlugin implements IModPlugin {
    private static final Identifier UID = Identifier.fromNamespaceAndPath(CFMain.MODID, "jei_plugin");

    @Override
    public Identifier getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ItemStackTemplate facadeResult = templateOf(CFItems.FACADE.get(), 1, Component.translatable("cable_facades.jei.facade_any_block").withStyle(ChatFormatting.RESET));

        registration.addRecipes(RecipeTypes.CRAFTING, Collections.singletonList(new RecipeHolder<>(
                ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(CFMain.MODID, "facade_crafting")),
                new ShapelessRecipe(
                        new Recipe.CommonInfo(true),
                        new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.BUILDING, "facades"),
                        facadeResult,
                        List.of(Ingredient.of(CFItems.FACADE.get()), Ingredient.of(Blocks.COBBLESTONE))
                )
        )));

        ItemStackTemplate directionalResult = templateOf(CFItems.DIRECTIONAL_FACADE.get(), 1, null);

        registration.addRecipes(RecipeTypes.CRAFTING, Collections.singletonList(new RecipeHolder<>(
                ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(CFMain.MODID, "directional_facade_crafting")),
                new ShapelessRecipe(
                        new Recipe.CommonInfo(true),
                        new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.BUILDING, "facades"),
                        directionalResult,
                        List.of(Ingredient.of(CFItems.DIRECTIONAL_FACADE.get()), Ingredient.of(Blocks.COBBLESTONE))
                )
        )));

        ItemStackTemplate conversionResult = templateOf(CFItems.DIRECTIONAL_FACADE.get(), 6, null);

        registration.addRecipes(RecipeTypes.CRAFTING, Collections.singletonList(new RecipeHolder<>(
                ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(CFMain.MODID, "facade_to_directional")),
                new ShapelessRecipe(
                        new Recipe.CommonInfo(true),
                        new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.BUILDING, "facades"),
                        conversionResult,
                        List.of(Ingredient.of(CFItems.FACADE.get()))
                )
        )));
    }

    private static ItemStackTemplate templateOf(Item item, int count, Component customName) {
        DataComponentPatch.Builder builder = DataComponentPatch.builder();
        builder.set(CFDataComponents.FACADE_BLOCK.get(), Optional.of(Blocks.COBBLESTONE));
        if (customName != null) {
            builder.set(DataComponents.CUSTOM_NAME, customName);
        }
        return new ItemStackTemplate(item.builtInRegistryHolder(), count, builder.build());
    }
}
