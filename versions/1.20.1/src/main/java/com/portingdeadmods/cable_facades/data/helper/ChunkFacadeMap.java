package com.portingdeadmods.cable_facades.data.helper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.utils.CodecUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.NotImplementedException;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ChunkFacadeMap {
    public static final Codec<ChunkFacadeMap> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.unboundedMap(Codec.STRING, FacadeData.CODEC).fieldOf("chunk_map").forGetter(ChunkFacadeMap::chunkMapToString)
    ).apply(builder, ChunkFacadeMap::chunkMapFromString));

    public static final Codec<ChunkFacadeMap> V2_MIGRATION_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.unboundedMap(Codec.STRING, CodecUtils.BLOCKSTATE_CODEC).fieldOf("chunk_map").forGetter(ChunkFacadeMap::chunkMapToV2String)
    ).apply(builder, ChunkFacadeMap::chunkMapFromV2String));

    public static final Codec<ChunkFacadeMap> MIGRATION_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.unboundedMap(Codec.STRING, CodecUtils.BLOCK_CODEC).fieldOf("chunk_map").forGetter(ChunkFacadeMap::chunkMapToOldString)
    ).apply(builder, ChunkFacadeMap::chunkMapFromOldString));

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
        return chunkMap.isEmpty();
    }

    private static boolean isParseableLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static ChunkFacadeMap chunkMapFromString(Map<String, FacadeData> in) {
        return new ChunkFacadeMap(in.entrySet().stream()
                .filter(e -> isParseableLong(e.getKey()))
                .map(e -> new AbstractMap.SimpleEntry<>(BlockPos.of(Long.parseLong(e.getKey())), e.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
    }

    private static ChunkFacadeMap chunkMapFromV2String(Map<String, BlockState> in) {
        return new ChunkFacadeMap(in.entrySet().stream()
                .filter(e -> isParseableLong(e.getKey()))
                .map(e -> new AbstractMap.SimpleEntry<>(BlockPos.of(Long.parseLong(e.getKey())), FacadeData.fullBlock(e.getValue())))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
    }

    private static ChunkFacadeMap chunkMapFromOldString(Map<String, Block> in) {
        return new ChunkFacadeMap(in.entrySet().stream()
                .filter(e -> isParseableLong(e.getKey()))
                .map(e -> new AbstractMap.SimpleEntry<>(BlockPos.of(Long.parseLong(e.getKey())), FacadeData.fullBlock(e.getValue().defaultBlockState())))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
    }

    private Map<String, FacadeData> chunkMapToString() {
        return chunkMap.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(String.valueOf(e.getKey().asLong()), e.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private Map<String, BlockState> chunkMapToV2String() {
        throw new NotImplementedException("V2 migration getter should never be called!");
    }

    private Map<String, Block> chunkMapToOldString() {
        throw new NotImplementedException("V1 migration getter should never be called!");
    }

    @Override
    public String toString() {
        return "ChunkFacadeMap{" + chunkMap + '}';
    }
}
