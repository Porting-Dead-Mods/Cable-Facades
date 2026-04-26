package com.portingdeadmods.cable_facades.utils;

import com.mojang.serialization.Codec;
import com.portingdeadmods.cable_facades.mixins.BlockStateBaseMixin;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CodecUtils {
    public static final Codec<Block> BLOCK_CODEC = registryCodec(BuiltInRegistries.BLOCK);
    public static final StreamCodec<ByteBuf, Block> BLOCK_STREAM_CODEC = registryStreamCodec(BuiltInRegistries.BLOCK);
    
    public static final Codec<BlockState> BLOCKSTATE_CODEC = blockStateCodec();
    public static final StreamCodec<ByteBuf, BlockState> BLOCKSTATE_STREAM_CODEC = blockStateStreamCodec();

    public static final StreamCodec<ByteBuf, ChunkPos> CHUNK_POS_STREAM_CODEC = ByteBufCodecs.VAR_LONG.map(ChunkPos::unpack, ChunkPos::pack);

    /**
     * Returns a codec using the resource location of the registry
     */
    public static <T> Codec<T> registryCodec(Registry<T> registry) {
        return Identifier.CODEC.xmap(registry::getValue, registry::getKey);
    }

    /**
     * Returns a stream codec using the resource location of the registry
     */
    public static <T> StreamCodec<ByteBuf, T> registryStreamCodec(Registry<T> registry) {
        return Identifier.STREAM_CODEC.map(registry::getValue, registry::getKey);
    }

    /**
     * Returns a codec using NBT as a helper
     */
    public static Codec<BlockState> blockStateCodec() {
        return BlockState.CODEC;
//        return TagParser.LENIENT_CODEC.xmap(
//            (state) -> NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), state),
//            NbtUtils::writeBlockState
//        );
    }

    /**
     * Returns a stream codec using NBT as a helper
     */
    public static StreamCodec<ByteBuf, BlockState> blockStateStreamCodec() {
        return ByteBufCodecs.fromCodec(BlockState.CODEC);
//        return ByteBufCodecs.COMPOUND_TAG.map(
//            (state) -> NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), state),
//            NbtUtils::writeBlockState
//        );
    }

}
