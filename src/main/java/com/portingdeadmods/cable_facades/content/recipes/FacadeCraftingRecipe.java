package com.portingdeadmods.cable_facades.content.recipes;

import com.mojang.serialization.MapCodec;
import com.portingdeadmods.cable_facades.content.items.FacadeItem;
import com.portingdeadmods.cable_facades.registries.CFDataComponents;
import com.portingdeadmods.cable_facades.registries.CFRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class FacadeCraftingRecipe extends CustomRecipe {
    public static final FacadeCraftingRecipe INSTANCE = new FacadeCraftingRecipe();
    public static final MapCodec<FacadeCraftingRecipe> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, FacadeCraftingRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<FacadeCraftingRecipe> SERIALIZER = new RecipeSerializer<>(FacadeCraftingRecipe.CODEC, FacadeCraftingRecipe.STREAM_CODEC);

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
    public @NotNull ItemStack assemble(CraftingInput craftingInput) {
        Block facadeBlock = null;
        ItemStack originalFacadeStack = ItemStack.EMPTY;
        ItemStack facadeStack = ItemStack.EMPTY;
        for (int i = 0; i < craftingInput.size(); i++) {
            ItemStack item = craftingInput.getItem(i);
            if (item.getItem() instanceof BlockItem blockItem) {
                facadeBlock = blockItem.getBlock();
                if(facadeBlock.defaultBlockState().getRenderShape() == RenderShape.INVISIBLE){
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
            if (optionalBlock.isPresent() && optionalBlock.get().asItem() instanceof BlockItem blockItem) {
                originalFacadeStack.set(CFDataComponents.HAS_FACADE_REMAINDER, true);
                return blockItem.getDefaultInstance();
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public @NonNull RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }
}
