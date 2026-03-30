package com.portingdeadmods.cable_facades.utils;

import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.data.FacadeData;
import com.portingdeadmods.cable_facades.networking.s2c.AddDirectionalFacadePayload;
import com.portingdeadmods.cable_facades.networking.s2c.AddFacadePayload;
import com.portingdeadmods.cable_facades.networking.s2c.RemoveDirectionalFacadePayload;
import com.portingdeadmods.cable_facades.networking.s2c.RemoveFacadePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public class FacadeUtils {
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
        if (level instanceof ServerLevel serverLevel) {
            CableFacadeSavedData.get(serverLevel).addFacade(pos, blockState);
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), new AddFacadePayload(pos, FacadeData.fullBlock(blockState)));
        }
    }

    public static void addDirectionalFacade(Level level, BlockPos pos, Direction face, BlockState blockState) {
        if (level instanceof ServerLevel serverLevel) {
            CableFacadeSavedData.get(serverLevel).addDirectionalFacade(pos, face, blockState);
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), new AddDirectionalFacadePayload(pos, face, blockState));
        }
    }

    public static void removeFacade(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            CableFacadeSavedData.get(serverLevel).removeFacade(pos);
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), new RemoveFacadePayload(pos));
        }
    }

    public static void removeDirectionalFacade(Level level, BlockPos pos, Direction face) {
        if (level instanceof ServerLevel serverLevel) {
            CableFacadeSavedData.get(serverLevel).removeDirectionalFacade(pos, face);
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), new RemoveDirectionalFacadePayload(pos, face));
        }
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
            level.updateNeighborsAtExceptFromFacing(pos, state.getBlock(), null);
            level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE);
            level.getLightEngine().checkBlock(pos);
            level.getChunkAt(pos).setUnsaved(true);
        } else {
            markSectionDirty(pos);
            level.getLightEngine().checkBlock(pos);
        }
    }

    public static void updateClientBlock(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            updateBlocks(minecraft.level, pos);
        }
    }

    public static void markSectionDirty(BlockPos pos) {
        SectionPos section = SectionPos.of(pos);
        Minecraft.getInstance().levelRenderer.setSectionDirty(section.x(), section.y(), section.z());
    }
}
