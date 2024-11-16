package com.portingdeadmods.cable_facades.events;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ClientCamoManager {
    public static Map<BlockPos, @Nullable Block> CAMOUFLAGED_BLOCKS = new Object2ObjectOpenHashMap<>();
}
