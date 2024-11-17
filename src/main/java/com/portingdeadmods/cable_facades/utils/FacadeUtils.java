package com.portingdeadmods.cable_facades.utils;

import com.portingdeadmods.cable_facades.data.CableFacadeSavedData;
import com.portingdeadmods.cable_facades.events.ClientCamoManager;
import com.portingdeadmods.cable_facades.networking.ModMessages;
import com.portingdeadmods.cable_facades.networking.s2c.AddFacadePacket;
import com.portingdeadmods.cable_facades.networking.s2c.RemoveFacadePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class FacadeUtils {
    public static boolean hasFacade(BlockGetter level, BlockPos pos) {
        return getFacade(level, pos) != null;
    }

    public static Block getFacade(BlockGetter level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            return CableFacadeSavedData.get(serverLevel).getCamouflagedBlocks().get(pos);
        }
        return ClientCamoManager.CAMOUFLAGED_BLOCKS.get(pos);
    }

    public static void addFacade(Level level, BlockPos pos, Block block) {
        if (level instanceof ServerLevel serverLevel) {
            CableFacadeSavedData.get(serverLevel).put(pos, block);
        }
        ModMessages.sendToClients(new AddFacadePacket(pos, block));
    }

    public static void removeFacade(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            CableFacadeSavedData.get(serverLevel).remove(pos);
        }
        ModMessages.sendToClients(new RemoveFacadePacket(pos));
    }
}
