package com.portingdeadmods.cable_facades.content.recipes;

import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import com.portingdeadmods.cable_facades.registries.CFRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FacadeCraftingRecipe extends CustomRecipe {
    public FacadeCraftingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        boolean hasBlock = false;
        boolean hasFacade = false;
        ItemStack facadeStack = null;

        for (int i = 0; i < craftingInput.size(); i++) {
            ItemStack stack = craftingInput.getItem(i);
            Item item = stack.getItem();
            if (item instanceof FacadeItem) {
                if (!hasFacade) {
                    facadeStack = stack.copy();
                    hasFacade = true;
                } else{
                    return false;
                }
            } else if (item instanceof BlockItem) {
                if (!hasBlock) {
                    hasBlock = true;
                } else {
                    return false;
                }
            } else if (!stack.isEmpty()) {
                return false;
            }
        }

        if (hasFacade && !hasBlock) {
            if (facadeStack.getOrDefault(CFDataComponents.FACADE_BLOCK, Optional.empty()).isPresent()) {
                return true;
            }
        }

        return hasFacade && hasBlock;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider registryAccess) {
        Block facadeBlock = null;
        ItemStack originalFacadeStack = ItemStack.EMPTY;
        ItemStack facadeStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingInput.size(); i++) {
            ItemStack item = craftingInput.getItem(i);
            if (item.getItem() instanceof BlockItem blockItem) {
                facadeBlock = blockItem.getBlock();
                if(facadeBlock.defaultBlockState().getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED){
                    return ItemStack.EMPTY;
                }
            } else if (item.getItem() instanceof FacadeItem) {
                facadeStack = item.copy();
                originalFacadeStack = item;
            }
        }

        if (!facadeStack.isEmpty() && facadeBlock != null) {
            ItemStack stack = facadeStack.getItem().getDefaultInstance();
            stack.set(CFDataComponents.FACADE_BLOCK, Optional.of(facadeBlock));
            return stack;
        } else if (!facadeStack.isEmpty()) {
            Optional<Block> optionalBlock = facadeStack.get(CFDataComponents.FACADE_BLOCK);
            if (optionalBlock.isPresent()) {
                originalFacadeStack.set(CFDataComponents.HAS_FACADE_REMAINDER, true);
                return optionalBlock.get().asItem().getDefaultInstance();
            }
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
