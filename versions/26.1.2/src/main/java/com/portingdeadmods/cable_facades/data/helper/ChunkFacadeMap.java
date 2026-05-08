package com.portingdeadmods.cable_facades.data.helper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.portingdeadmods.cable_facades.data.FacadeData;
import net.minecraft.core.BlockPos;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ChunkFacadeMap {
    public static final Codec<ChunkFacadeMap> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.unboundedMap(Codec.STRING, FacadeData.CODEC).fieldOf("chunk_map").forGetter(ChunkFacadeMap::chunkMapToString)
    ).apply(builder, ChunkFacadeMap::chunkMapFromString));

    private final Map<BlockPos, FacadeData> chunkMap;

    public ChunkFacadeMap() {
        this.chunkMap = new HashMap<>();
    }

    public ChunkFacadeMap(Map<BlockPos, FacadeData> chunkMap) {
        this.chunkMap = chunkMap;
    }

    public Map<BlockPos, FacadeData> getChunkMap() {
        return chunkMap;
    }

    public boolean isEmpty() {
        return getChunkMap().isEmpty();
    }

    private static ChunkFacadeMap chunkMapFromString(Map<String, FacadeData> chunkFacade) {
        return new ChunkFacadeMap(chunkFacade.entrySet().stream()
                .filter(entry -> {
                    try {
                        Long.parseLong(entry.getKey());
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .map(entry -> new AbstractMap.SimpleEntry<>(BlockPos.of(Long.parseLong(entry.getKey())), entry.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
    }

    private Map<String, FacadeData> chunkMapToString() {
        return getChunkMap().entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(String.valueOf(entry.getKey().asLong()), entry.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    @Override
    public String toString() {
        return "ChunkFacadeMap{" +
                "chunkMap=" + chunkMap +
                '}';
    }
}
