package com.portingdeadmods.cable_facades.utils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public final class FacadeItemNbt {
    public static final String KEY_FACADE_BLOCK = "FacadeBlock";
    public static final String KEY_HAS_REMAINDER = "HasFacadeRemainder";

    private FacadeItemNbt() {}

    @Nullable
    public static Block getFacadeBlock(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(KEY_FACADE_BLOCK)) return null;
        ResourceLocation rl = ResourceLocation.tryParse(tag.getString(KEY_FACADE_BLOCK));
        if (rl == null) return null;
        Block block = BuiltInRegistries.BLOCK.get(rl);
        return block == Blocks.AIR ? null : block;
    }

    public static void setFacadeBlock(ItemStack stack, @Nullable Block block) {
        CompoundTag tag = stack.getOrCreateTag();
        if (block == null) {
            tag.remove(KEY_FACADE_BLOCK);
        } else {
            ResourceLocation rl = BuiltInRegistries.BLOCK.getKey(block);
            tag.putString(KEY_FACADE_BLOCK, rl.toString());
        }
    }

    public static boolean hasRemainder(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(KEY_HAS_REMAINDER);
    }

    public static void setHasRemainder(ItemStack stack, boolean value) {
        if (value) {
            stack.getOrCreateTag().putBoolean(KEY_HAS_REMAINDER, true);
        } else if (stack.getTag() != null) {
            stack.getTag().remove(KEY_HAS_REMAINDER);
        }
    }
}
