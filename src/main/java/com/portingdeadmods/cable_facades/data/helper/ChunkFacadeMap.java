package com.portingdeadmods.cable_facades.data.helper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
            Codec.unboundedMap(Codec.STRING, CodecUtils.BLOCKSTATE_CODEC).fieldOf("chunk_map").forGetter(ChunkFacadeMap::chunkMapToString)
    ).apply(builder, ChunkFacadeMap::chunkMapFromString));

    //This codec will be used to attempt migration
    public static final Codec<ChunkFacadeMap> MIGRATION_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.unboundedMap(Codec.STRING, CodecUtils.BLOCK_CODEC).fieldOf("chunk_map").forGetter(ChunkFacadeMap::chunkMapToOldString)
    ).apply(builder, ChunkFacadeMap::chunkMapFromOldString));

    private final Map<BlockPos, BlockState> chunkMap;

    public ChunkFacadeMap() {
        this.chunkMap = new HashMap<>();
    }

    public ChunkFacadeMap(Map<BlockPos, BlockState> chunkMap) {
        this.chunkMap = chunkMap;
    }

    public Map<BlockPos, BlockState> getChunkMap() {
        return chunkMap;
    }

    private static ChunkFacadeMap chunkMapFromString(Map<String, BlockState> chunkFacade) {
        return new ChunkFacadeMap(chunkFacade.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(BlockPos.of(Long.parseLong(entry.getKey())), entry.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
    }

    //This method is used only if migration is required. Converts Blocks to BlockStates
    private static ChunkFacadeMap chunkMapFromOldString(Map<String, Block> chunkFacade) {
        return new ChunkFacadeMap(chunkFacade.entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(BlockPos.of(Long.parseLong(entry.getKey())), entry.getValue().defaultBlockState()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
    }

    private Map<String, BlockState> chunkMapToString() {
        return getChunkMap().entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(String.valueOf(entry.getKey().asLong()), entry.getValue()))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    //Unused and should never be called!
    private Map<String, Block> chunkMapToOldString() {
        throw new NotImplementedException("This method shouldn't be called!");
    }

    @Override
    public String toString() {
        return "ChunkFacadeMap{" +
                "chunkMap=" + chunkMap +
                '}';
    }
}
