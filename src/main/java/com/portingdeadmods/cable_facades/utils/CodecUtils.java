package com.portingdeadmods.cable_facades.utils;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class CodecUtils {
    public static final Codec<Block> BLOCK_CODEC = registryCodec(BuiltInRegistries.BLOCK);

    /**
     * Returns a codec using the resource location of the registry
     */
    public static <T> Codec<T> registryCodec(Registry<T> registry) {
        return ResourceLocation.CODEC.xmap(registry::get, registry::getKey);
    }
}
