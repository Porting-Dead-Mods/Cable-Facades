package com.portingdeadmods.cable_facades.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.portingdeadmods.cable_facades.CFMain;
import com.portingdeadmods.cable_facades.data.helper.ChunkFacadeMap;
import com.portingdeadmods.cable_facades.data.helper.LevelFacadeMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * This saved data saves all facades based on chunks.
 * In future versions this system will be replaced with chunk data attachments.
 * <br>
 * Internally it uses a hashmap that maps {@link ChunkPos} to {@link ChunkFacadeMap}
 * <br>
 * {@link ChunkFacadeMap} maps individual {@link BlockPos}itions to {@link Block}
 */
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
        ChunkPos chunkPos = new ChunkPos(blockPos.getX(), blockPos.getY());
        return getOrCreateFacadeMapForChunk(chunkPos);
    }

    public @Nullable ChunkFacadeMap getFacadeMapForPos(BlockPos blockPos) {
        ChunkPos chunkPos = new ChunkPos(blockPos.getX(), blockPos.getY());
        return getFacadeMapForChunk(chunkPos);
    }

    public void addFacade(BlockPos blockPos, Block block) {
        getOrCreateFacadeMapForPos(blockPos).getChunkMap().put(blockPos, block);
        setDirty();
    }

    public void removeFacade(BlockPos blockPos) {
        getOrCreateFacadeMapForPos(blockPos).getChunkMap().remove(blockPos);
        setDirty();
    }

    public boolean isEmpty() {
        return this.levelFacadeMap.getChunkFacadeMaps().isEmpty();
    }

    public @Nullable Block getFacade(BlockPos blockPos) {
        ChunkFacadeMap facadeMapForPos = getFacadeMapForPos(blockPos);
        if (facadeMapForPos != null) {
            return facadeMapForPos.getChunkMap().get(blockPos);
        }
        return null;
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
}