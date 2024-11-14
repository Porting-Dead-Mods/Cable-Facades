package com.portingdeadmods.cable_facades.content.recipes;

import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.registries.CFItems;
import com.portingdeadmods.cable_facades.registries.CFRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class FacadeCraftingRecipe extends CustomRecipe {
    public FacadeCraftingRecipe(ResourceLocation p_252125_, CraftingBookCategory p_249010_) {
        super(p_252125_, p_249010_);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        boolean hasBlock = false;
        boolean hasFacade = false;

        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack stack = craftingContainer.getItem(i);
            Item item = stack.getItem();
            if (item instanceof FacadeItem)
                hasFacade = true;
            else if (item instanceof BlockItem)
                hasBlock = true;
            else if (!stack.isEmpty())
                return false;
        }
        return hasFacade && hasBlock;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        Block facadeBlock = null;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
            if (item.getItem() instanceof BlockItem blockItem) {
                facadeBlock = blockItem.getBlock();
            } else if (item.getItem() instanceof FacadeItem) {
                itemStack = CFItems.FACADE.get().getDefaultInstance();
            }
        }
        if (!itemStack.isEmpty()) {
            itemStack.getOrCreateTag().putString(FacadeItem.FACADE_BLOCK, BuiltInRegistries.BLOCK.getKey(facadeBlock).toString());
            return itemStack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return CFRecipes.FACADE.get();
    }
}
