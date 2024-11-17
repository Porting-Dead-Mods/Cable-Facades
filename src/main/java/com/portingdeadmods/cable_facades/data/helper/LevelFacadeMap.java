package com.portingdeadmods.cable_facades.data.helper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.ChunkPos;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LevelFacadeMap {
    public static final Codec<LevelFacadeMap> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.unboundedMap(Codec.STRING, ChunkFacadeMap.CODEC).fieldOf("chunks_map").forGetter(LevelFacadeMap::levelFacadeMapToString)
    ).apply(builder, LevelFacadeMap::levelFacadeMapFromString));

    private final Map<ChunkPos, ChunkFacadeMap> chunkFacadeMaps;

    public LevelFacadeMap() {
        this.chunkFacadeMaps = new HashMap<>();
    }

    public LevelFacadeMap(Map<ChunkPos, ChunkFacadeMap> chunkFacadeMaps) {
        this.chunkFacadeMaps = chunkFacadeMaps;
    }

    public Map<ChunkPos, ChunkFacadeMap> getChunkFacadeMaps() {
        return chunkFacadeMaps;
    }

    public static LevelFacadeMap levelFacadeMapFromString(Map<String, ChunkFacadeMap> chunkFacade) {
        return new LevelFacadeMap(chunkFacade.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(new ChunkPos(Long.parseLong(entry.getKey())), entry.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
    }

    public Map<String, ChunkFacadeMap> levelFacadeMapToString() {
        return getChunkFacadeMaps().entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(String.valueOf(entry.getKey().toLong()), entry.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }
}
