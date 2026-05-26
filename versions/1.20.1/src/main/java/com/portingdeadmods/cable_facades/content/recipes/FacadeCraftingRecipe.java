package com.portingdeadmods.cable_facades.content.recipes;

import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.registries.CFRecipes;
import com.portingdeadmods.cable_facades.utils.FacadeItemNbt;
import net.minecraft.core.RegistryAccess;
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

public class FacadeCraftingRecipe extends CustomRecipe {

    public FacadeCraftingRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        boolean hasBlock = false;
        boolean hasFacade = false;
        ItemStack facadeStack = null;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            Item item = stack.getItem();
            if (item instanceof FacadeItem) {
                if (!hasFacade) {
                    facadeStack = stack.copy();
                    hasFacade = true;
                } else {
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
            return facadeStack != null && FacadeItemNbt.getFacadeBlock(facadeStack) != null;
        }
        return hasFacade && hasBlock;
    }

    @Override
    public @NotNull ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        Block facadeBlock = null;
        ItemStack originalFacadeStack = ItemStack.EMPTY;
        ItemStack facadeStack = ItemStack.EMPTY;

        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack item = container.getItem(i);
            if (item.getItem() instanceof BlockItem blockItem) {
                facadeBlock = blockItem.getBlock();
                if (facadeBlock.defaultBlockState().getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED) {
                    return ItemStack.EMPTY;
                }
            } else if (item.getItem() instanceof FacadeItem) {
                facadeStack = item.copy();
                originalFacadeStack = item;
            }
        }

        if (!facadeStack.isEmpty() && facadeBlock != null) {
            ItemStack stack = facadeStack.getItem().getDefaultInstance();
            FacadeItemNbt.setFacadeBlock(stack, facadeBlock);
            return stack;
        } else if (!facadeStack.isEmpty()) {
            Block existingBlock = FacadeItemNbt.getFacadeBlock(facadeStack);
            if (existingBlock != null && existingBlock.asItem() instanceof BlockItem blockItem) {
                FacadeItemNbt.setHasRemainder(originalFacadeStack, true);
                return blockItem.getDefaultInstance();
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return CFRecipes.FACADE.get();
    }
}
