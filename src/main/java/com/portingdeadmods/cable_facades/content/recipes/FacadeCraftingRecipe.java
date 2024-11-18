package com.portingdeadmods.cable_facades.content.recipes;

import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import com.portingdeadmods.cable_facades.registries.CFItems;
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

        for (int i = 0; i < craftingInput.size(); i++) {
            ItemStack stack = craftingInput.getItem(i);
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
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider registryAccess) {
        Block facadeBlock = null;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingInput.size(); i++) {
            ItemStack item = craftingInput.getItem(i);
            if (item.getItem() instanceof BlockItem blockItem) {
                facadeBlock = blockItem.getBlock();
                if(facadeBlock.defaultBlockState().getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED){
                    return ItemStack.EMPTY;
                }
            } else if (item.getItem() instanceof FacadeItem) {
                itemStack = CFItems.FACADE.get().getDefaultInstance();
            }
        }
        if (!itemStack.isEmpty()) {
            itemStack.set(CFDataComponents.FACADE_BLOCK, Optional.ofNullable(facadeBlock));
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
