package com.portingdeadmods.cable_facades.data.helper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.portingdeadmods.cable_facades.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ChunkFacadeMap {
    public static final Codec<ChunkFacadeMap> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.unboundedMap(Codec.STRING, CodecUtils.BLOCK_CODEC).fieldOf("chunk_map").forGetter(ChunkFacadeMap::chunkMapToString)
    ).apply(builder, ChunkFacadeMap::chunkMapFromString));

    private final Map<BlockPos, Block> chunkMap;

    public ChunkFacadeMap() {
        this.chunkMap = new HashMap<>();
    }

    public ChunkFacadeMap(Map<BlockPos, Block> chunkMap) {
        this.chunkMap = chunkMap;
    }

    public Map<BlockPos, Block> getChunkMap() {
        return chunkMap;
    }

    public boolean isEmpty() {
        return getChunkMap().isEmpty();
    }

    private static ChunkFacadeMap chunkMapFromString(Map<String, Block> chunkFacade) {
        return new ChunkFacadeMap(chunkFacade.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(BlockPos.of(Long.parseLong(entry.getKey())), entry.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
    }

    private Map<String, Block> chunkMapToString() {
        return getChunkMap().entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(String.valueOf(entry.getKey().asLong()), entry.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }
}
