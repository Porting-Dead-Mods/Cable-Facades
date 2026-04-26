package com.portingdeadmods.cable_facades.utils;

import com.portingdeadmods.cable_facades.data.FacadeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public final class ClientFacadeManager {
    private static final Map<BlockPos, FacadeData> FACADED_BLOCKS = new ConcurrentHashMap<>();
    private static final Map<ChunkPos, List<BlockPos>> LOADED_BLOCKS = new ConcurrentHashMap<>();

    public static void put(BlockPos pos, FacadeData data) {
        FACADED_BLOCKS.put(pos, data);
    }

    @Nullable
    public static FacadeData get(BlockPos pos) {
        if (pos == null) return null;
        return FACADED_BLOCKS.get(pos);
    }

    @Nullable
    public static BlockState getFullBlock(BlockPos pos) {
        FacadeData data = FACADED_BLOCKS.get(pos);
        return data != null ? data.getFullBlock() : null;
    }

    public static void remove(BlockPos pos) {
        FACADED_BLOCKS.remove(pos);
    }

    public static boolean containsPos(BlockPos pos) {
        return FACADED_BLOCKS.containsKey(pos);
    }

    public static boolean isEmpty() {
        return FACADED_BLOCKS.isEmpty();
    }

    public static void putAll(Map<BlockPos, FacadeData> map) {
        FACADED_BLOCKS.putAll(map);
    }

    public static void forEach(BiConsumer<BlockPos, FacadeData> action) {
        FACADED_BLOCKS.forEach(action);
    }

    public static Stream<Map.Entry<BlockPos, FacadeData>> entryStream() {
        return FACADED_BLOCKS.entrySet().stream();
    }

    public static Iterable<Map.Entry<BlockPos, FacadeData>> entryIterator() {
        return FACADED_BLOCKS.entrySet();
    }

    public static void clear() {
        FACADED_BLOCKS.clear();
        LOADED_BLOCKS.clear();
    }

    public static boolean containsChunk(ChunkPos chunkPos) {
        return LOADED_BLOCKS.containsKey(chunkPos);
    }

    public static void trackChunk(ChunkPos chunkPos, List<BlockPos> positions) {
        LOADED_BLOCKS.put(chunkPos, new ArrayList<>(positions));
    }

    public static void untrackChunk(ChunkPos chunkPos) {
        List<BlockPos> positions = LOADED_BLOCKS.remove(chunkPos);
        if (positions != null) {
            for (BlockPos pos : positions) {
                FACADED_BLOCKS.remove(pos);
            }
        }
    }

    public static void addDirectionalFacade(BlockPos pos, Direction face, BlockState state) {
        FACADED_BLOCKS.compute(pos, (k, existing) -> {
            if (existing != null && existing.isDirectional()) {
                return existing.withFace(face, state);
            }
            return FacadeData.directional(face, state);
        });
    }

    public static void removeDirectionalFacade(BlockPos pos, Direction face) {
        FACADED_BLOCKS.compute(pos, (k, existing) -> {
            if (existing != null && existing.isDirectional()) {
                return existing.withoutFace(face);
            }
            return null;
        });
    }
}
