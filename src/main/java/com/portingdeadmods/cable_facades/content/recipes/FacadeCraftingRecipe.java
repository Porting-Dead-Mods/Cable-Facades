package com.portingdeadmods.cable_facades.content.recipes;

import com.portingdeadmods.cable_facades.content.items.FacadeItem;
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
import net.minecraft.world.level.block.RenderShape;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FacadeCraftingRecipe extends CustomRecipe {
    public FacadeCraftingRecipe(ResourceLocation p_252125_, CraftingBookCategory p_249010_) {
        super(p_252125_, p_249010_);
    }

    @Override
    public boolean matches(CraftingContainer craftingContainer, Level level) {
        boolean hasBlock = false;
        boolean hasFacade = false;
        ItemStack facadeStack = null;

        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack stack = craftingContainer.getItem(i);
            Item item = stack.getItem();
            if (item instanceof FacadeItem facadeItem) {
                facadeStack = stack.copy();
                hasFacade = true;
            } else if (item instanceof BlockItem) {
                hasBlock = true;
            } else if (!stack.isEmpty()) {
                return false;
            }
        }

        if (hasFacade && !hasBlock) {
            if (facadeStack.getTag().contains(FacadeItem.FACADE_BLOCK)) {
                return true;
            }
        }

        return hasFacade && hasBlock;
    }

    @Override
    public ItemStack assemble(CraftingContainer craftingContainer, RegistryAccess registryAccess) {
        Block facadeBlock = null;
        ItemStack itemStack = ItemStack.EMPTY;
        ItemStack originalFacadeStack = ItemStack.EMPTY;
        ItemStack facadeStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingContainer.getContainerSize(); i++) {
            ItemStack item = craftingContainer.getItem(i);
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
            stack.getOrCreateTag().putString(FacadeItem.FACADE_BLOCK, BuiltInRegistries.BLOCK.getKey(facadeBlock).toString());
            return stack;
        } else if (!facadeStack.isEmpty()) {
            Optional<Block> optionalBlock = facadeStack.getTag().contains(FacadeItem.FACADE_BLOCK) ? Optional.ofNullable(BuiltInRegistries.BLOCK.get(new ResourceLocation(facadeStack.getTag().getString(FacadeItem.FACADE_BLOCK)))) : Optional.empty();
            if (optionalBlock.isPresent()) {
                originalFacadeStack.getTag().putBoolean("has_facade_remainder", true);
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
