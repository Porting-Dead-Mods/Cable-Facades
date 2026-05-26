package com.portingdeadmods.cable_facades.data.helper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.portingdeadmods.cable_facades.data.FacadeData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LevelFacadeMap {
    public static final Codec<LevelFacadeMap> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.unboundedMap(Codec.STRING, ChunkFacadeMap.CODEC).fieldOf("chunks_map").forGetter(LevelFacadeMap::levelFacadeMapToString)
    ).apply(builder, LevelFacadeMap::levelFacadeMapFromString));

    public static final Codec<LevelFacadeMap> V2_MIGRATION_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.unboundedMap(Codec.STRING, ChunkFacadeMap.V2_MIGRATION_CODEC).fieldOf("chunks_map").forGetter(LevelFacadeMap::levelFacadeMapToString)
    ).apply(builder, LevelFacadeMap::levelFacadeMapFromString));

    public static final Codec<LevelFacadeMap> MIGRATION_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.unboundedMap(Codec.STRING, ChunkFacadeMap.MIGRATION_CODEC).fieldOf("chunks_map").forGetter(LevelFacadeMap::levelFacadeMapToString)
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

    public static LevelFacadeMap levelFacadeMapFromString(Map<String, ChunkFacadeMap> in) {
        return new LevelFacadeMap(in.entrySet().stream()
                .filter(e -> {
                    try {
                        Long.parseLong(e.getKey());
                        return true;
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                })
                .map(e -> new AbstractMap.SimpleEntry<>(new ChunkPos(Long.parseLong(e.getKey())), e.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
    }

    public Map<String, ChunkFacadeMap> levelFacadeMapToString() {
        return chunkFacadeMaps.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(String.valueOf(e.getKey().toLong()), e.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    public Map<BlockPos, FacadeData> getAllFacades() {
        Map<BlockPos, FacadeData> all = new HashMap<>();
        for (ChunkFacadeMap m : chunkFacadeMaps.values()) {
            all.putAll(m.getChunkMap());
        }
        return all;
    }

    public Map<BlockPos, BlockState> getAllFullFacadeStates() {
        Map<BlockPos, BlockState> all = new HashMap<>();
        for (ChunkFacadeMap m : chunkFacadeMaps.values()) {
            m.getChunkMap().forEach((pos, fd) -> {
                BlockState s = fd.getFullBlock();
                if (s != null) all.put(pos, s);
            });
        }
        return all;
    }

    @Override
    public String toString() {
        return "LevelFacadeMap{" + chunkFacadeMaps + '}';
    }
}
