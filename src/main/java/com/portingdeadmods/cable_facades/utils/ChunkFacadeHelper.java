package com.portingdeadmods.cable_facades.utils;

import com.portingdeadmods.cable_facades.data.ChunkFacadeMap;
import com.portingdeadmods.cable_facades.registries.CFDataAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ChunkFacadeHelper {
    private final ServerLevel serverLevel;

    private ChunkFacadeHelper(ServerLevel serverLevel) {
        this.serverLevel = serverLevel;
    }

    public static ChunkFacadeHelper get(ServerLevel serverLevel) {
        return new ChunkFacadeHelper(serverLevel);
    }

    public @NotNull ChunkFacadeMap getFacadeMapForChunk(ChunkPos chunkPos) {
        return this.serverLevel.getChunk(new BlockPos(chunkPos.x, 0, chunkPos.z)).getData(CFDataAttachments.FACADES);
    }

    public @NotNull ChunkFacadeMap getFacadeMapForPos(BlockPos blockPos) {
        ChunkPos chunkPos = new ChunkPos(blockPos.getX(), blockPos.getY());
        return getFacadeMapForChunk(chunkPos);
    }

    public void addFacade(BlockPos blockPos, Block block) {
        Map<BlockPos, Block> chunkMap = getFacadeMapForPos(blockPos).getChunkMap();
        chunkMap.put(blockPos, block);
        this.serverLevel.getChunk(blockPos).setData(CFDataAttachments.FACADES, new ChunkFacadeMap(chunkMap));
    }

    public void removeFacade(BlockPos blockPos) {
        Map<BlockPos, Block> chunkMap = getFacadeMapForPos(blockPos).getChunkMap();
        chunkMap.remove(blockPos);
        this.serverLevel.getChunk(blockPos).setData(CFDataAttachments.FACADES, new ChunkFacadeMap(chunkMap));
    }

    public @Nullable Block getFacade(BlockPos blockPos) {
        ChunkFacadeMap facadeMapForPos = getFacadeMapForPos(blockPos);
        return facadeMapForPos.getChunkMap().get(blockPos);
    }
}
