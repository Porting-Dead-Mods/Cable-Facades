package com.portingdeadmods.cable_facades.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.portingdeadmods.cable_facades.CFMain;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NetworkingUtils {
    public static void writeBlockState(FriendlyByteBuf buf, BlockState blockState) {
        Optional<Tag> tag = CodecUtils.BLOCKSTATE_CODEC.encodeStart(NbtOps.INSTANCE, blockState).result();
        if (tag.isPresent()) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.put("blockState", tag.get());
            buf.writeNbt(compoundTag);
        }
    }

    public static Map<BlockPos, BlockState> getFacades(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<BlockPos, BlockState> facades = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            BlockPos pos = buf.readBlockPos();
            BlockState blockState = readBlockState(buf);
            if (blockState != null) {
                facades.put(pos, blockState);
            }
        }
        return facades;
    }

    public static BlockState readBlockState(FriendlyByteBuf buf) {
        Tag tag = buf.readNbt().get("blockState");
        DataResult<Pair<BlockState, Tag>> dataResult = CodecUtils.BLOCKSTATE_CODEC.decode(NbtOps.INSTANCE, tag);
        Optional<Pair<BlockState, Tag>> optionalBlockState = dataResult.result();
        if (optionalBlockState.isPresent()) {
            return optionalBlockState.get().getFirst();
        }
        // If this is reached, we have errored
        CFMain.LOGGER.error("Failed to decode blockstate from bytebuf: {}", dataResult.error().get().message());
        return null;
    }
}
