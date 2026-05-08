package com.portingdeadmods.cable_facades.data;

import com.mojang.serialization.Codec;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.data.helper.ChunkFacadeMap;
import com.portingdeadmods.cable_facades.data.helper.LevelFacadeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CableFacadeSavedData extends SavedData {
    public static final Codec<CableFacadeSavedData> CODEC = LevelFacadeMap.CODEC.xmap(
            CableFacadeSavedData::new,
            CableFacadeSavedData::getLevelFacadeMap
    );

    public static final SavedDataType<CableFacadeSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(CFMain.MODID, "cable_facades_saved_data"),
            CableFacadeSavedData::new,
            CODEC
    );

    private final LevelFacadeMap levelFacadeMap;

    public CableFacadeSavedData(LevelFacadeMap levelFacadeMap) {
        this.levelFacadeMap = levelFacadeMap;
    }

    public CableFacadeSavedData() {
        this(new LevelFacadeMap());
    }

    public LevelFacadeMap getLevelFacadeMap() {
        return this.levelFacadeMap;
    }

    public @NotNull ChunkFacadeMap getOrCreateFacadeMapForChunk(ChunkPos chunkPos) {
        ChunkFacadeMap map = getFacadeMapForChunk(chunkPos);
        if (map == null) {
            map = new ChunkFacadeMap();
            this.levelFacadeMap.getChunkFacadeMaps().put(chunkPos, map);
            setDirty();
        }
        return map;
    }

    public @Nullable ChunkFacadeMap getFacadeMapForChunk(ChunkPos chunkPos) {
        return this.levelFacadeMap.getChunkFacadeMaps().get(chunkPos);
    }

    public @NotNull ChunkFacadeMap getOrCreateFacadeMapForPos(BlockPos blockPos) {
        return getOrCreateFacadeMapForChunk(ChunkPos.containing(blockPos));
    }

    public @Nullable ChunkFacadeMap getFacadeMapForPos(BlockPos blockPos) {
        return getFacadeMapForChunk(ChunkPos.containing(blockPos));
    }

    public void addFacade(BlockPos blockPos, BlockState blockState) {
        addFacade(blockPos, FacadeData.fullBlock(blockState));
    }

    public void addFacade(BlockPos blockPos, FacadeData facadeData) {
        getOrCreateFacadeMapForPos(blockPos).getChunkMap().put(blockPos, facadeData);
        setDirty();
    }

    public void addDirectionalFacade(BlockPos blockPos, Direction direction, BlockState blockState) {
        addDirectionalFacade(blockPos, direction, blockState, FacadeTypes.DEFAULT_ID);
    }

    public void addDirectionalFacade(BlockPos blockPos, Direction direction, BlockState blockState, Identifier facadeType) {
        ChunkFacadeMap chunkMap = getOrCreateFacadeMapForPos(blockPos);
        FacadeData existing = chunkMap.getChunkMap().get(blockPos);
        FacadeData updated;
        if (existing != null && existing.isDirectional()) {
            updated = existing.withFace(direction, blockState);
        } else {
            updated = FacadeData.directional(facadeType, direction, blockState);
        }
        chunkMap.getChunkMap().put(blockPos, updated);
        setDirty();
    }

    public void removeDirectionalFacade(BlockPos blockPos, Direction direction) {
        ChunkFacadeMap chunkMap = getOrCreateFacadeMapForPos(blockPos);
        FacadeData existing = chunkMap.getChunkMap().get(blockPos);
        if (existing != null && existing.isDirectional()) {
            FacadeData updated = existing.withoutFace(direction);
            if (updated == null) {
                chunkMap.getChunkMap().remove(blockPos);
            } else {
                chunkMap.getChunkMap().put(blockPos, updated);
            }
            setDirty();
        }
    }

    public void removeFacade(BlockPos blockPos) {
        getOrCreateFacadeMapForPos(blockPos).getChunkMap().remove(blockPos);
        setDirty();
    }

    public boolean isEmpty() {
        return this.levelFacadeMap.getChunkFacadeMaps().isEmpty();
    }

    @Nullable
    public FacadeData getFacadeData(BlockPos blockPos) {
        ChunkFacadeMap facadeMapForPos = getFacadeMapForPos(blockPos);
        if (facadeMapForPos != null) {
            return facadeMapForPos.getChunkMap().get(blockPos);
        }
        return null;
    }

    @Nullable
    public BlockState getFacade(BlockPos blockPos) {
        FacadeData data = getFacadeData(blockPos);
        return data != null ? data.getFullBlock() : null;
    }

    public static CableFacadeSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    @Override
    public String toString() {
        return "CableFacadeSavedData{" +
                "levelFacadeMap=" + levelFacadeMap +
                '}';
    }
}
