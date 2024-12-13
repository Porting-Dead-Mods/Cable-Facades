package com.portingdeadmods.cable_facades.utils;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class CodecUtils {
    public static final Codec<Block> BLOCK_CODEC = registryCodec(BuiltInRegistries.BLOCK);

    public static final Codec<BlockState> BLOCKSTATE_CODEC = blockStateCodec();

    /**
     * Returns a codec using the resource location of the registry
     */
    public static <T> Codec<T> registryCodec(Registry<T> registry) {
        return ResourceLocation.CODEC.xmap(registry::get, registry::getKey);
    }

    /**
     * Returns a codec using NBT as a helper
     */
    public static Codec<BlockState> blockStateCodec() {
        return CompoundTag.CODEC.xmap(
                (state) -> NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), state),
                NbtUtils::writeBlockState
        );
    }
}
