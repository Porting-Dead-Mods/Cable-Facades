package com.portingdeadmods.cable_facades.utils;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;

public class CodecUtils {
    public static final Codec<Block> BLOCK_CODEC = registryCodec(BuiltInRegistries.BLOCK);
    public static final StreamCodec<ByteBuf, Block> BLOCK_STREAM_CODEC = registryStreamCodec(BuiltInRegistries.BLOCK);

    public static final StreamCodec<ByteBuf, ChunkPos> CHUNK_POS_STREAM_CODEC = ByteBufCodecs.VAR_LONG.map(ChunkPos::new, ChunkPos::toLong);

    /**
     * Returns a codec using the resource location of the registry
     */
    public static <T> Codec<T> registryCodec(Registry<T> registry) {
        return ResourceLocation.CODEC.xmap(registry::get, registry::getKey);
    }

    /**
     * Returns a stream codec using the resource location of the registry
     */
    public static <T> StreamCodec<ByteBuf, T> registryStreamCodec(Registry<T> registry) {
        return ResourceLocation.STREAM_CODEC.map(registry::get, registry::getKey);
    }
}
