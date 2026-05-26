package com.portingdeadmods.cable_facades.utils;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CodecUtils {
    public static final Codec<Block> BLOCK_CODEC = registryCodec(BuiltInRegistries.BLOCK);
    public static final Codec<BlockState> BLOCKSTATE_CODEC = blockStateCodec();

    private CodecUtils() {}

    public static <T> Codec<T> registryCodec(Registry<T> registry) {
        return ResourceLocation.CODEC.xmap(registry::get, registry::getKey);
    }

    public static Codec<BlockState> blockStateCodec() {
        return CompoundTag.CODEC.xmap(
                state -> NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), state),
                NbtUtils::writeBlockState
        );
    }

    public static void writeBlockState(FriendlyByteBuf buf, BlockState state) {
        buf.writeNbt(NbtUtils.writeBlockState(state));
    }

    public static BlockState readBlockState(FriendlyByteBuf buf) {
        CompoundTag tag = buf.readNbt();
        if (tag == null) return Blocks.AIR.defaultBlockState();
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag);
    }
}
