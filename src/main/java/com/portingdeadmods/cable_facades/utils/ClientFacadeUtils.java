package com.portingdeadmods.cable_facades.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ClientFacadeUtils {
    public static void updateBlocks(BlockPos pos) {
        Level level = Minecraft.getInstance().level;
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, 3);
        level.updateNeighborsAt(pos, state.getBlock());

        level.getLightEngine().checkBlock(pos);
    }
}
