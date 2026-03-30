package com.portingdeadmods.cable_facades.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.data.helper.ChunkFacadeMap;
import com.portingdeadmods.cable_facades.data.helper.LevelFacadeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CableFacadeSavedData extends SavedData {
    public static final String ID = "cable_facades_saved_data";

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
        return getOrCreateFacadeMapForChunk(new ChunkPos(blockPos));
    }

    public @Nullable ChunkFacadeMap getFacadeMapForPos(BlockPos blockPos) {
        return getFacadeMapForChunk(new ChunkPos(blockPos));
    }

    public void addFacade(BlockPos blockPos, BlockState blockState) {
        addFacade(blockPos, FacadeData.fullBlock(blockState));
    }

    public void addFacade(BlockPos blockPos, FacadeData facadeData) {
        getOrCreateFacadeMapForPos(blockPos).getChunkMap().put(blockPos, facadeData);
        setDirty();
    }

    public void addDirectionalFacade(BlockPos blockPos, Direction direction, BlockState blockState) {
        ChunkFacadeMap chunkMap = getOrCreateFacadeMapForPos(blockPos);
        FacadeData existing = chunkMap.getChunkMap().get(blockPos);
        FacadeData updated;
        if (existing != null && existing.isDirectional()) {
            updated = existing.withFace(direction, blockState);
        } else {
            updated = FacadeData.directional(direction, blockState);
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

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        DataResult<Tag> tagDataResult = LevelFacadeMap.CODEC.encodeStart(NbtOps.INSTANCE, this.levelFacadeMap);
        tagDataResult
                .resultOrPartial(err -> CFMain.LOGGER.error("Encoding error: {}", err))
                .ifPresent(tag -> compoundTag.put(ID, tag));
        return compoundTag;
    }

    private static CableFacadeSavedData load(CompoundTag compoundTag, ServerLevel serverLevel) {
        DataResult<Pair<LevelFacadeMap, Tag>> dataResult = LevelFacadeMap.CODEC.decode(NbtOps.INSTANCE, compoundTag.get(ID));
        Optional<Pair<LevelFacadeMap, Tag>> mapTagPair = dataResult
                .resultOrPartial(err -> CFMain.LOGGER.error("Decoding error: {}", err));

        if (dataResult.error().isPresent()) {
            CFMain.LOGGER.error("Data may be outdated - attempting V2 migration (BlockState format)!");
            dataResult = LevelFacadeMap.V2_MIGRATION_CODEC.decode(NbtOps.INSTANCE, compoundTag.get(ID));
            mapTagPair = dataResult
                    .resultOrPartial(err -> CFMain.LOGGER.error("V2 migration error: {}", err));
        }

        if (dataResult.error().isPresent()) {
            CFMain.LOGGER.error("V2 migration failed - attempting V1 migration (Block format)!");
            dataResult = LevelFacadeMap.MIGRATION_CODEC.decode(NbtOps.INSTANCE, compoundTag.get(ID));
            mapTagPair = dataResult
                    .resultOrPartial(err -> CFMain.LOGGER.error("V1 migration failed: {}", err));
        }

        if (mapTagPair.isPresent()) {
            LevelFacadeMap facadeMap = mapTagPair.get().getFirst();
            return new CableFacadeSavedData(facadeMap);
        }
        return new CableFacadeSavedData();
    }

    public static CableFacadeSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(level), ID);
    }

    private static SavedData.Factory<CableFacadeSavedData> factory(ServerLevel pLevel) {
        return new SavedData.Factory<>(CableFacadeSavedData::new, (tag, provider) -> load(tag, pLevel));
    }

    @Override
    public String toString() {
        return "CableFacadeSavedData{" +
                "levelFacadeMap=" + levelFacadeMap +
                '}';
    }
}
