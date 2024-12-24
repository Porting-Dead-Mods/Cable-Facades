package com.portingdeadmods.cable_facades.events;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClientFacadeManager {
    // Blocks in the currently loaded chunks. Updated whenever a chunk is loaded or unloaded by this player
    public static Map<BlockPos, @Nullable BlockState> FACADED_BLOCKS = new Object2ObjectOpenHashMap<>();
    // Map to keep track of what blockpositions are in which chunks
    public static final Map<ChunkPos, List<BlockPos>> LOADED_BLOCKS = new Object2ObjectOpenHashMap<>();
}
