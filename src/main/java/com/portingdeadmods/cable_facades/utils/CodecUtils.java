package com.portingdeadmods.cable_facades.utils;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

public class CodecUtils {
    public static final Codec<BlockState> BLOCKSTATE_CODEC = blockStateCodec();
    public static final StreamCodec<ByteBuf, BlockState> BLOCKSTATE_STREAM_CODEC = blockStateStreamCodec();

    public static final StreamCodec<ByteBuf, ChunkPos> CHUNK_POS_STREAM_CODEC = ByteBufCodecs.VAR_LONG.map(ChunkPos::new, ChunkPos::toLong);

    /**
     * Returns a codec using NBT as a helper
     */
    public static Codec<BlockState> blockStateCodec() {
        return TagParser.LENIENT_CODEC.xmap(
            (state) -> NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), state),
            NbtUtils::writeBlockState
        );
    }

    /**
     * Returns a stream codec using NBT as a helper
     */
    public static StreamCodec<ByteBuf, BlockState> blockStateStreamCodec() {
        return ByteBufCodecs.COMPOUND_TAG.map(
            (state) -> NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), state),
            NbtUtils::writeBlockState
        );
    }

}
