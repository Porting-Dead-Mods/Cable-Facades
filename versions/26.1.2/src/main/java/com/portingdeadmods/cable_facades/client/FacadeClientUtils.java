package com.portingdeadmods.cable_facades.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class FacadeClientUtils {
    private FacadeClientUtils() {
    }

    public static void updateClientBlock(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null || !level.isInWorldBounds(pos)) {
            return;
        }

        level.getLightEngine().checkBlock(pos);
        BlockPos immutablePos = pos.immutable();
        var state = level.getBlockState(immutablePos);
        level.sendBlockUpdated(immutablePos, state, state, Block.UPDATE_CLIENTS);

        SectionPos section = SectionPos.of(immutablePos);
        minecraft.levelRenderer.setSectionDirty(section.x(), section.y(), section.z());
    }
}
