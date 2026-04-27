package com.portingdeadmods.cable_facades.utils;

import com.portingdeadmods.cable_facades.api.facade_type.FacadeTypes;
import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.networking.CFMessages;
import com.portingdeadmods.cable_facades.networking.s2c.AddDirectionalFacadePacket;
import com.portingdeadmods.cable_facades.networking.s2c.AddFacadePacket;
import com.portingdeadmods.cable_facades.networking.s2c.RemoveDirectionalFacadePacket;
import com.portingdeadmods.cable_facades.networking.s2c.RemoveFacadePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public final class FacadeUtils {
    private FacadeUtils() {}

    public static boolean hasFacade(BlockGetter level, BlockPos pos) {
        return getFacadeData(level, pos) != null;
    }

    @Nullable
    public static FacadeData getFacadeData(BlockGetter level, BlockPos pos) {
        if (level == null || pos == null) return null;
        if (level instanceof ServerLevel serverLevel) {
            return CableFacadeSavedData.get(serverLevel).getFacadeData(pos);
        }
        return ClientFacadeManager.get(pos);
    }

    @Nullable
    public static BlockState getFacade(BlockGetter level, BlockPos pos) {
        FacadeData data = getFacadeData(level, pos);
        return data != null ? data.getFullBlock() : null;
    }

    @Nullable
    public static BlockState getDirectionalFacade(BlockGetter level, BlockPos pos, Direction direction) {
        FacadeData data = getFacadeData(level, pos);
        if (data != null && data.isDirectional()) {
            return data.getFace(direction);
        }
        return null;
    }

    public static void addFacade(Level level, BlockPos pos, BlockState blockState) {
        addFacade(level, pos, blockState, FacadeTypes.DEFAULT_ID);
    }

    public static void addFacade(Level level, BlockPos pos, BlockState blockState, ResourceLocation facadeType) {
        if (level instanceof ServerLevel serverLevel) {
            FacadeData data = FacadeData.fullBlock(facadeType, blockState);
            CableFacadeSavedData.get(serverLevel).addFacade(pos, data);
            sendToChunk(serverLevel, pos, new AddFacadePacket(pos, data));
        }
    }

    public static void addDirectionalFacade(Level level, BlockPos pos, Direction face, BlockState blockState) {
        addDirectionalFacade(level, pos, face, blockState, FacadeTypes.DEFAULT_ID);
    }

    public static void addDirectionalFacade(Level level, BlockPos pos, Direction face, BlockState blockState, ResourceLocation facadeType) {
        if (level instanceof ServerLevel serverLevel) {
            CableFacadeSavedData.get(serverLevel).addDirectionalFacade(pos, face, blockState, facadeType);
            sendToChunk(serverLevel, pos, new AddDirectionalFacadePacket(pos, face, blockState, facadeType));
        }
    }

    public static void removeFacade(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            CableFacadeSavedData.get(serverLevel).removeFacade(pos);
            sendToChunk(serverLevel, pos, new RemoveFacadePacket(pos));
        }
    }

    public static void removeDirectionalFacade(Level level, BlockPos pos, Direction face) {
        if (level instanceof ServerLevel serverLevel) {
            CableFacadeSavedData.get(serverLevel).removeDirectionalFacade(pos, face);
            sendToChunk(serverLevel, pos, new RemoveDirectionalFacadePacket(pos, face));
        }
    }

    private static void sendToChunk(ServerLevel level, BlockPos pos, Object packet) {
        LevelChunk chunk = level.getChunkAt(pos);
        CFMessages.sendToChunk(packet, chunk);
    }

    public static void updateBlocks(Level level, BlockPos pos) {
        if (level == null || !level.isInWorldBounds(pos)) return;

        BlockState state = level.getBlockState(pos);

        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) != null) {
                level.getBlockEntity(pos).setChanged();
                level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            }
            level.updateNeighborsAt(pos, state.getBlock());
            level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE);
            level.getLightEngine().checkBlock(pos);
            level.getChunkAt(pos).setUnsaved(true);
        } else {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            level.getLightEngine().checkBlock(pos);
        }
    }
}
